package bleach.hack.util.shader;

// all my homies hate importing resources from files
public class StaticShaders {
	
	/** Entity outline shader without any blurring **/
	public static final String OUTLINE_SHADER = "{\"targets\":[\"swap\",\"final\"],\"passes\":[{\"name\":\"entity_outline\",\"intarget\":\"final\",\"outtarget\":\"swap\"},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"final\"}]}";
	
	/** Entity outline with blur (Format: %1 = Range, %2 = Blur range) **/
	public static final String MC_SHADER_UNFOMATTED = "{\"targets\":[\"swap\",\"final\"],\"passes\":[{\"name\":\"entity_outline\",\"intarget\":\"final\",\"outtarget\":\"swap\"},{\"name\":\"blur\",\"intarget\":\"swap\",\"outtarget\":\"final\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[%2,0]},{\"name\":\"Radius\",\"values\":[%1]}]},{\"name\":\"blur\",\"intarget\":\"final\",\"outtarget\":\"swap\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[0,%2]},{\"name\":\"Radius\",\"values\":[%1]}]},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"final\"}]}";

}
