import javax.sound.sampled.*;

class MicManager {

	public static class ThreadMic extends Thread {
		public boolean running = true;
		@Override
		public void run() {
			AudioFormat format = new AudioFormat(44100,16,2,true,true);
			DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,format);
			DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,format);
			try {
				TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
				targetLine.open(format);
				targetLine.start();

				SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getMixer(Main.getInfoCable()).getLine(sourceInfo);
				sourceLine.open(format);
				sourceLine.start();

				int numBytesRead;
				byte[] targetData = new byte[targetLine.getBufferSize() / 5];

				while (running) {
					numBytesRead = targetLine.read(targetData, 0, targetData.length);
					if (numBytesRead == -1)	break;
					sourceLine.write(targetData, 0, numBytesRead);
				}
//				sourceLine.drain();
				sourceLine.close();
//				targetLine.drain();
				targetLine.close();
			}
			catch (Exception e) {
				System.out.println("Exception in mic thread");
				e.printStackTrace();
			}
		}
	}
}
