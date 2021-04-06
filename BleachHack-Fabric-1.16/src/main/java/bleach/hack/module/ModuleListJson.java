package bleach.hack.module;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ModuleListJson {

	@SerializedName("package")
	private String packageName;

	@SerializedName("modules")
	private List<String> modules;

	public String getPackage() {
		return this.packageName;
	}

	public List<String> getModules() {
		return this.modules;
	}
}
