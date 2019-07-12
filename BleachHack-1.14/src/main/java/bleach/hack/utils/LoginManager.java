package bleach.hack.utils;

import java.net.Proxy;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
			auth.logIn();
			
			Session newsession = new Session(auth.getSelectedProfile().getName(),
					auth.getSelectedProfile().getId().toString(),
					auth.getAuthenticatedToken(), "mojang");
			
			ObfuscationReflectionHelper.findField(Minecraft.getInstance().getClass(), "field_71449_j")
				.set(Minecraft.getInstance(), newsession);
			return "§aLogin Successful";
		
		}catch (IllegalAccessException | SecurityException e) 
		{
			return "§cInternal Error (" + e.toString() + ")";
			
		}catch(AuthenticationException e)
		{
			e.printStackTrace();
			if(e.getMessage().contains("Invalid username or password.")
				|| e.getMessage().toLowerCase().contains("account migrated"))
				return "§4Wrong password!";
			else
				return "§cCannot contact authentication server!";
			
		}catch(NullPointerException e)
		{
			return "§4Wrong password!";
			
		}
	}
}
