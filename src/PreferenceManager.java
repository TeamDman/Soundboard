import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.util.prefs.Preferences;

@SuppressWarnings("SpellCheckingInspection")
class PreferenceManager {
	private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);
	static boolean alwaysOnTop;
	static boolean autoRelay;
	static int     dividerX;
	static int     windowX, windowY, windowW, windowH;

	public static void init() {
		Main.currentDir = new File(prefs.get("startdir", "%userprofile%"));
		Main.updateGains(prefs.getFloat("gain", 1f));
		autoRelay = prefs.getBoolean("autorelay", false);
		alwaysOnTop = prefs.getBoolean("alwaysontop", false);
		windowX = prefs.getInt("windowx", 0);
		windowY = prefs.getInt("windowy", 0);
		windowW = prefs.getInt("windoww", -1);
		windowH = prefs.getInt("windowh", -1);
		dividerX = prefs.getInt("dividerx", -1);
		for (Main.EnumKeyAction action : Main.EnumKeyAction.values())
			action.setKey(prefs.getInt(action.name() + "key", -1), prefs.get(action.name() + "keyName", "undefined"));

		Mixer.Info[] infos        = AudioSystem.getMixerInfo();
		String       nameCable    = prefs.get("nameCable", null);
		String       nameSpeakers = prefs.get("nameSpeakers", null);
		String       nameMic      = prefs.get("nameMic", null);
		if (nameCable == null)
			Main.setInfoCable(infos[0]);
		if (nameSpeakers == null)
			Main.setInfoSpeakers(infos[0]);
		if (nameMic == null)
			Main.setInfoMic(infos[0]);
		for (Mixer.Info v : infos) {
			if (v.getName().equals(nameCable))
				Main.setInfoCable(v);
			if (v.getName().equals(nameSpeakers))
				Main.setInfoSpeakers(v);
			if (v.getName().equals(nameMic))
				Main.setInfoMic(v);
		}

		if (Main.window != null)
			Main.window.updateStatus("Prefs loaded");
	}

	public static void save() {
		try {
			prefs.put("startdir", Main.currentDir.getAbsolutePath());
			prefs.putFloat("gain", Main.getGain());
			prefs.putBoolean("autorelay", autoRelay);
			prefs.put("nameCable", Main.getInfoCable().getName());
			prefs.put("nameSpeakers", Main.getInfoSpeakers().getName());
			prefs.put("nameMic", Main.getInfoMic().getName());
			for (Main.EnumKeyAction action : Main.EnumKeyAction.values()) {
				prefs.putInt(action.name() + "key", action.getKey());
				prefs.put(action.name() + "keyName", action.getKeyName());
			}

			if (Main.window != null) {
				prefs.putInt("windowx", Main.window.getX());
				prefs.putInt("windowy", Main.window.getY());
				prefs.putInt("windoww", Main.window.getWidth());
				prefs.putInt("windowh", Main.window.getHeight());
				prefs.putInt("dividerx", Main.window.getDividerX());
				Main.window.updateStatus("Prefs saved");
			}
		} catch (Exception e) {
			System.out.println("Error saving prefs");
			e.printStackTrace();
		}
	}
}
