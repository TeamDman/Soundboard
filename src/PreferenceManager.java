import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.util.prefs.Preferences;

public class PreferenceManager {
	private static Preferences prefs = Preferences.userNodeForPackage(Main.class);
	public static void init() {
		Main.startDir = new File(prefs.get("startdir","%userprofile%"));
		for (Main.EnumKeyAction action : Main.EnumKeyAction.values())
			Main.setBinding(action,prefs.getInt(action.name()+"key",-1),prefs.get(action.name()+"keyName","undefined"));
		Main.setGain(prefs.getFloat("gain",1f),true);
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		String nameCable = prefs.get("nameCable",null);
		String nameSpeakers = prefs.get("nameSpeakers", null);
		if (nameCable == null) {
			Main.setInfoCable(infos[0]);
		}
		if (nameSpeakers == null) {
			Main.setInfoSpeakers(infos[0]);
		}
		for (Mixer.Info v : infos) {
			if (v.getName().equals(nameCable))
				Main.setInfoCable(v);
			else if (v.getName().equals(nameSpeakers))
				Main.setInfoSpeakers(v);
		}
		System.out.println("Prefs loaded");
	}

	public static void save() {
		if (Main.window != null)
			Main.window.setStatus("Saving prefs");
		prefs.put("startdir",Main.startDir.getAbsolutePath());
		for (Main.EnumKeyAction action : Main.EnumKeyAction.values()){
			prefs.putInt(action.name()+"key",action.getKey());
			prefs.put(action.name()+"keyName",action.getKeyName());
		}
		prefs.putFloat("gain",Main.getGain());
		prefs.put("nameCable",Main.getInfoCable().getName());
		prefs.put("nameSpeakers",Main.getInfoSpeakers().getName());

	}
}
