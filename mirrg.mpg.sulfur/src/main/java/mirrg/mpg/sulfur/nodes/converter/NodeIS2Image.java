package mirrg.mpg.sulfur.nodes.converter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import mirrg.helium.standard.hydrogen.struct.Tuple3;
import mirrg.mpg.sulfur.node.Node;
import mirrg.mpg.sulfur.node.pin.PinInput;
import mirrg.mpg.sulfur.node.pin.PinOutput;

public class NodeIS2Image extends Node
{

	private ArrayList<Tuple3<byte[], Integer, Integer>> buffers = new ArrayList<>();

	public final PinInput<Tuple3<byte[], Integer, Integer>> pinInput;
	public final PinOutput<BufferedImage> pinOutput;

	public NodeIS2Image()
	{
		addPinInput(pinInput = new PinInput<Tuple3<byte[], Integer, Integer>>() {

			@Override
			protected void onClosed()
			{
				try {
					byte[] buffer = new byte[buffers.stream()
						.mapToInt(b -> b.getZ())
						.sum()];
					int[] off = new int[] {
						0,
					};
					buffers.forEach(b -> {
						System.arraycopy(b.getX(), b.getY(), buffer, off[0], b.getZ());
						off[0] += b.getZ();
					});

					BufferedImage image = ImageIO.read(new InputStream() {

						private int i;

						@Override
						public int read() throws IOException
						{
							if (i >= buffer.length) return -1;
							int b = Byte.toUnsignedInt(buffer[i]);
							i++;
							return b;
						}

					});
					pinOutput.fire(image);
					pinOutput.fireClose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void accept(Tuple3<byte[], Integer, Integer> packet)
			{
				buffers.add(packet);
			}

		});
		addPinOutput(pinOutput = new PinOutput<>());
	}

}
