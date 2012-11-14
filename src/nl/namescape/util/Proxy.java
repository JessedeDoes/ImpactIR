package nl.namescape.util;
import java.net.Authenticator;
import java.net.PasswordAuthentication;






public class Proxy 
{

	public static class httpAuthenticateProxy extends Authenticator 
	{
		protected PasswordAuthentication getPasswordAuthentication() 
		{
			// username, password
			// sets http authentication
			return new PasswordAuthentication("does", "e2e4.e7e5".toCharArray());
		}
	}

	public static void setProxy()
	{
		System.setProperty("http.proxyHost", "proxy.inl.nl"); 
		System.setProperty("http.proxyPort",  "8080");


		//System.out.println("using proxy: "+ SecureClient.proxyhost + " port " + SecureClient.proxyport);


		// now create http authentication


		// this didn't work 
		// System.setProperty("http.proxyUser", "myuser"); 
		// System.setProperty("http.proxyPassword", "mypassword");


		// this worked in 1.4.1 
		Authenticator.setDefault( new httpAuthenticateProxy() ); 
	}
}
