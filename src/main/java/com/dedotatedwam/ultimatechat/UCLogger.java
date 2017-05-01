package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.config.UCConfig;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.source.ConsoleSource;

public class UCLogger{

	//TODO Convert this entire class to proper Logger format
	private ConsoleSource console;
	
	UCLogger(Server serv){
		this.console = serv.getConsole();
	}
	 
	public void logClear(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
	
	public void sucess(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
    
    public void warning(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (UCConfig.getInstance() != null && UCConfig.getInstance().getBool("debug-messages")) {
        	console.sendMessage(UCUtil.toText("UltimateChat: [&b"+s+"&r]"));
        }  
    }
}
