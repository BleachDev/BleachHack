package bleach.hack.utils;

import java.lang.reflect.Field;
import java.net.Proxy;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class LoginManager {

	public static String login(String email, String password)
	{
		YggdrasilUserAuthentication auth =
			(YggdrasilUserAuthentication)new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);
		
		auth.setUsername(email);
		auth.setPassword(password);
		
		try
		{
			Field session = Minecraft.getInstance().getClass().getDeclaredField("session");
			
			Session newsession = new Session(auth.getSelectedProfile().getName(),
					auth.getSelectedProfile().getId().toString(),
					auth.getAuthenticatedToken(), "mojang");
			
			FieldUtils.writeField(session, Minecraft.getInstance(), newsession, true);
			return "§aLogin Successful";
		
		}catch (IllegalAccessException | NoSuchFieldException | SecurityException e) 
		{
			return "§cInternal Error (" + e.toString() + ")";
			
		}catch(NullPointerException e)
		{
			return "§4Wrong password!";
			
		}catch(Exception e)
		{
			return "§ci don't even know anymore";
		}
	}
}
