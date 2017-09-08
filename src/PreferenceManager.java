import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.util.prefs.Preferences;

@SuppressWarnings("SpellCheckingInspection")
class PreferenceManager {
	private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);
	static boolean alwaysOnTop;
	static boolean autoRelay;

	public static void init() {
		Main.startDir = new File(prefs.get("startdir", "%userprofile%"));
		Main.setGain(prefs.getFloat("gain", 1f), true);
		autoRelay = prefs.getBoolean("autorelay", false);
		alwaysOnTop=prefs.getBoolean("alwaysontop",false);
		for (Main.EnumKeyAction action : Main.EnumKeyAction.values())
			Main.setBinding(action, prefs.getInt(action.name() + "key", -1), prefs.get(action.name() + "keyName", "undefined"));

		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		String nameCable = prefs.get("nameCable", null);
		String nameSpeakers = prefs.get("nameSpeakers", null);
		if (nameCable == null)
			Main.setInfoCable(infos[0]);
		if (nameSpeakers == null)
			Main.setInfoSpeakers(infos[0]);
		for (Mixer.Info v : infos) {
			if (v.getName().equals(nameCable))
				Main.setInfoCable(v);
			else if (v.getName().equals(nameSpeakers))
				Main.setInfoSpeakers(v);
		}

		if (Main.window != null)
			Main.window.setStatus("Prefs loaded");
	}

	public static void save() {
		prefs.put("startdir", Main.startDir.getAbsolutePath());
		prefs.putFloat("gain", Main.getGain());
		prefs.putBoolean("autorelay", autoRelay);
		prefs.put("nameCable", Main.getInfoCable().getName());
		prefs.put("nameSpeakers", Main.getInfoSpeakers().getName());
		for (Main.EnumKeyAction action : Main.EnumKeyAction.values()) {
			prefs.putInt(action.name() + "key", action.getKey());
			prefs.put(action.name() + "keyName", action.getKeyName());
		}

		if (Main.window != null)
			Main.window.setStatus("Prefs saved");
	}
}
