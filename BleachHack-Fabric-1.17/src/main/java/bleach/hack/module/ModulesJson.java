package bleach.hack.module;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModulesJson {

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
