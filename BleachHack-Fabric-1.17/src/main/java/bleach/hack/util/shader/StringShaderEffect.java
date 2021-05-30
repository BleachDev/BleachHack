/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.shader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Matrix4f;

public class StringShaderEffect extends ShaderEffect {

	private final Framebuffer mainTarget;
	private final ResourceManager resourceManager;
	private final List<PostProcessShader> passes = Lists.newArrayList();
	private final Map<String, Framebuffer> targetsByName = Maps.newHashMap();
	private final List<Framebuffer> defaultSizedTargets = Lists.newArrayList();
	private Matrix4f projectionMatrix;
	private int width;
	private int height;
	private float time;
	private float lastTickDelta;

	public StringShaderEffect(Framebuffer framebuffer, ResourceManager resourceMang, TextureManager textureManager, String jsonString) throws JsonSyntaxException, IOException {
		this(framebuffer, resourceMang, textureManager, JsonHelper.deserialize(jsonString));
	}

	public StringShaderEffect(Framebuffer framebuffer, ResourceManager resourceMang, TextureManager textureManager, JsonObject jsonObject) throws JsonSyntaxException, IOException {
		super(null, null, framebuffer, new Identifier(""));
		this.resourceManager = resourceMang;
		this.mainTarget = framebuffer;
		this.time = 0.0F;
		this.lastTickDelta = 0.0F;
		this.width = framebuffer.viewportWidth;
		this.height = framebuffer.viewportHeight;
		this.setupProjectionMatrix();
		parseEffect(textureManager, jsonObject);
	}

	private void parseEffect(TextureManager textureManager, JsonObject jsonObject) throws ShaderParseException {
		try {
			if (JsonHelper.hasArray(jsonObject, "targets")) {
				for(JsonElement je: jsonObject.getAsJsonArray("targets")) {
					try {
						parseTarget(je);
					} catch (Exception var17) {
						ShaderParseException shaderParseException2 = ShaderParseException.wrap(var17);
						shaderParseException2.addFaultyElement("targets[" + je + "]");
						throw shaderParseException2;
					}
				}
			}

			if (JsonHelper.hasArray(jsonObject, "passes")) {
				for (JsonElement je: jsonObject.getAsJsonArray("passes")) {
					try {
						parsePass(textureManager, je);
						//System.out.println("PASS >> " + je);
					} catch (Exception var16) {
						ShaderParseException shaderParseException2 = ShaderParseException.wrap(var16);
						shaderParseException2.addFaultyElement("passes[" + je + "]");
						throw shaderParseException2;
					}
				}
			}
		} catch (Exception var18) {
			throw ShaderParseException.wrap(var18);
		}
	}

	private void parseTarget(JsonElement jsonTarget) throws ShaderParseException {
		if (JsonHelper.isString(jsonTarget)) {
			this.addTarget(jsonTarget.getAsString(), this.width, this.height);
		} else {
			JsonObject jsonObject = JsonHelper.asObject(jsonTarget, "target");
			String string = JsonHelper.getString(jsonObject, "name");
			int i = JsonHelper.getInt(jsonObject, "width", this.width);
			int j = JsonHelper.getInt(jsonObject, "height", this.height);
			if (this.targetsByName.containsKey(string)) {
				throw new ShaderParseException(string + " is already defined");
			}

			this.addTarget(string, i, j);
		}

	}

	private void parsePass(TextureManager textureManager, JsonElement jsonPass) throws IOException {
		JsonObject jsonObject = JsonHelper.asObject(jsonPass, "pass");
		String string = JsonHelper.getString(jsonObject, "name");
		String string2 = JsonHelper.getString(jsonObject, "intarget");
		String string3 = JsonHelper.getString(jsonObject, "outtarget");
		Framebuffer framebuffer = this.getTarget(string2);
		Framebuffer framebuffer2 = this.getTarget(string3);
		if (framebuffer == null) {
			throw new ShaderParseException("Input target '" + string2 + "' does not exist");
		} else if (framebuffer2 == null) {
			throw new ShaderParseException("Output target '" + string3 + "' does not exist");
		} else {
			PostProcessShader postProcessShader = this.addPass(string, framebuffer, framebuffer2);
			JsonArray jsonArray = JsonHelper.getArray(jsonObject, "auxtargets", (JsonArray)null);
			if (jsonArray != null) {
				for (JsonElement jsonElement: jsonArray) {
					try {
						JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "auxtarget");
						String string4 = JsonHelper.getString(jsonObject2, "name");
						String string5 = JsonHelper.getString(jsonObject2, "id");
						boolean bl2;
						String string7;
						if (string5.endsWith(":depth")) {
							bl2 = true;
							string7 = string5.substring(0, string5.lastIndexOf(58));
						} else {
							bl2 = false;
							string7 = string5;
						}

						Framebuffer framebuffer3 = this.getTarget(string7);
						if (framebuffer3 == null) {
							if (bl2) {
								throw new ShaderParseException("Render target '" + string7 + "' can't be used as depth buffer");
							}

							Identifier identifier = new Identifier("textures/effect/" + string7 + ".png");
							Resource resource = null;

							try {
								resource = this.resourceManager.getResource(identifier);
							} catch (FileNotFoundException var31) {
								throw new ShaderParseException("Render target or texture '" + string7 + "' does not exist");
							} finally {
								IOUtils.closeQuietly(resource);
							}

							RenderSystem.setShaderTexture(0, identifier);
							textureManager.bindTexture(identifier);
							AbstractTexture abstractTexture = textureManager.getTexture(identifier);
							int j = JsonHelper.getInt(jsonObject2, "width");
							int k = JsonHelper.getInt(jsonObject2, "height");
							boolean var25 = JsonHelper.getBoolean(jsonObject2, "bilinear");
							if (var25) {
								RenderSystem.texParameter(3553, 10241, 9729);
								RenderSystem.texParameter(3553, 10240, 9729);
							} else {
								RenderSystem.texParameter(3553, 10241, 9728);
								RenderSystem.texParameter(3553, 10240, 9728);
							}

							postProcessShader.addAuxTarget(string4, abstractTexture::getGlId, j, k);
						} else if (bl2) {
							postProcessShader.addAuxTarget(string4, framebuffer3::getDepthAttachment, framebuffer3.textureWidth, framebuffer3.textureHeight);
						} else {
							postProcessShader.addAuxTarget(string4, framebuffer3::getColorAttachment, framebuffer3.textureWidth, framebuffer3.textureHeight);
						}
					} catch (Exception var33) {
						ShaderParseException shaderParseException = ShaderParseException.wrap(var33);
						shaderParseException.addFaultyElement("auxtargets[" + jsonElement + "]");
						throw shaderParseException;
					}
				}
			}

			JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
			if (jsonArray2 != null) {
				for(JsonElement jsonElement2: jsonArray2) {
					try {
						this.parseUniform(jsonElement2);
					} catch (Exception var30) {
						ShaderParseException shaderParseException2 = ShaderParseException.wrap(var30);
						shaderParseException2.addFaultyElement("uniforms[" + jsonElement2 + "]");
						throw shaderParseException2;
					}
				}
			}

		}
	}

	private void parseUniform(JsonElement jsonUniform) throws ShaderParseException {
		JsonObject jsonObject = JsonHelper.asObject(jsonUniform, "uniform");
		String string = JsonHelper.getString(jsonObject, "name");
		GlUniform glUniform = ((PostProcessShader)this.passes.get(this.passes.size() - 1)).getProgram().getUniformByName(string);
		if (glUniform == null) {
			throw new ShaderParseException("Uniform '" + string + "' does not exist");
		} else {
			float[] fs = new float[4];
			int i = 0;
			JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");

			for(JsonElement jsonElement: jsonArray) {
				try {
					fs[i] = JsonHelper.asFloat(jsonElement, "value");
				} catch (Exception var12) {
					ShaderParseException shaderParseException = ShaderParseException.wrap(var12);
					shaderParseException.addFaultyElement("values[" + i + "]");
					throw shaderParseException;
				}

				i++;
			}

			switch(i) {
				case 0:
				default:
					break;
				case 1:
					glUniform.set(fs[0]);
					break;
				case 2:
					glUniform.set(fs[0], fs[1]);
					break;
				case 3:
					glUniform.set(fs[0], fs[1], fs[2]);
					break;
				case 4:
					glUniform.set(fs[0], fs[1], fs[2], fs[3]);
			}

		}
	}

	public Framebuffer getSecondaryTarget(String name) {
		return (Framebuffer)this.targetsByName.get(name);
	}

	public void addTarget(String name, int width, int height) {
		Framebuffer framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
		framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.targetsByName.put(name, framebuffer);
		if (width == this.width && height == this.height) {
			this.defaultSizedTargets.add(framebuffer);
		}
	}

	public void close() {
		for (Framebuffer framebuffer: targetsByName.values()) {
			framebuffer.delete();
		}

		for (PostProcessShader postProcessShader: passes) {
			postProcessShader.close();
		}

		this.passes.clear();
	}

	public PostProcessShader addPass(String programName, Framebuffer source, Framebuffer dest) throws IOException {
		PostProcessShader postProcessShader = new PostProcessShader(this.resourceManager, programName, source, dest);
		this.passes.add(this.passes.size(), postProcessShader);
		return postProcessShader;
	}

	private void setupProjectionMatrix() {
		this.projectionMatrix = Matrix4f.projectionMatrix(0.0F, (float) this.mainTarget.textureWidth, (float) this.mainTarget.textureHeight, 0.0F, 0.1F, 1000.0F);
	}

	public void setupDimensions(int targetsWidth, int targetsHeight) {
		this.width = this.mainTarget.textureWidth;
		this.height = this.mainTarget.textureHeight;
		this.setupProjectionMatrix();

		for (PostProcessShader postProcessShader: passes) {
			postProcessShader.setProjectionMatrix(this.projectionMatrix);
		}

		for (Framebuffer framebuffer: defaultSizedTargets) {
			framebuffer.resize(targetsWidth, targetsHeight, MinecraftClient.IS_SYSTEM_MAC);
		}

	}

	public void render(float tickDelta) {
		if (tickDelta < this.lastTickDelta) {
			this.time += 1.0F - this.lastTickDelta;
			this.time += tickDelta;
		} else {
			this.time += tickDelta - this.lastTickDelta;
		}

		for (this.lastTickDelta = tickDelta; this.time > 20.0F; this.time -= 20.0F) {
		}

		for (PostProcessShader postProcessShader: passes) {
			postProcessShader.render(this.time / 20.0F);
		}

	}

	private Framebuffer getTarget(String name) {
		if (name == null) {
			return null;
		} else {
			return name.equals("minecraft:main") ? this.mainTarget : (Framebuffer)this.targetsByName.get(name);
		}
	}
}
