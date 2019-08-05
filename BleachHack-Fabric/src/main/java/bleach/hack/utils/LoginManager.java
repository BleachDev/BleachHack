package bleach.hack.utils;

import java.net.Proxy;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

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
			
			FabricReflect.writeField(MinecraftClient.getInstance(), newsession, "field_1726", "session");
			return "§aLogin Successful";
		
		}catch (SecurityException e) 
		{
			return "§cReflection Error";
			
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
