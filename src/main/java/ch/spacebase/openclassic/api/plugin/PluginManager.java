package ch.spacebase.openclassic.api.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.Yaml;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.Listener;
import ch.spacebase.openclassic.api.util.JarFilter;


public class PluginManager {
	
	private List<Plugin> plugins = new ArrayList<Plugin>();
	private Map<Listener, Plugin> listeners = new HashMap<Listener, Plugin>();

	public void loadPlugins() {
		File plugins = new File("plugins");
		if(!plugins.exists()) plugins.mkdirs();
		
		File jars[] = plugins.listFiles(new JarFilter());
		
		for(File file : jars) {	
			this.loadPlugin(file);
		}
		
		OpenClassic.getLogger().info(this.plugins.size() + " plugins loaded!");
	}
	
	public void loadPlugin(File file) {
		URL url = null;
		
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			OpenClassic.getLogger().severe("Malformed plugin jar URL!");
			e.printStackTrace();
			return;
		}
		
		PluginDescription description = this.getDescription(file);
		
		if(description == null) return;
		
		try {
			OpenClassic.getLogger().info(Color.GREEN + "Loading " + description.getFullName() + "...");
			
			URLClassLoader loader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());
			
	        Class<?> jarClass = Class.forName(description.getMainClass(), true, loader);
	        Class<? extends Plugin> plugin = jarClass.asSubclass(Plugin.class);
	
	        Constructor<? extends Plugin> constructor = plugin.getConstructor();
	
	        Plugin p = constructor.newInstance();
	        
	        boolean matched = true;
	        
	        for(String dependency : p.getDescription().getDependencies()) {
	        	if(!this.isPluginEnabled(dependency)) matched = false;
	        }
	        
	        if(matched) {
	        	this.enablePlugin(p);
	        }
	        
	        this.plugins.add(p);
		} catch(Exception e) {
			OpenClassic.getLogger().severe("Failed to load plugin from file " + file.getName() + "!");
			e.printStackTrace();
		}
	}
	
	public void enablePlugin(Plugin plugin) {
		if(plugin.isEnabled()) return;
		
		OpenClassic.getLogger().info(Color.GREEN + "Enabling " + plugin.getDescription().getFullName() + "...");
		
		plugin.setEnabled(true);
		plugin.onEnable();
		
		for(Plugin p : this.plugins) {
			if(!p.isEnabled()) {
				List<String> deps = Arrays.asList(p.getDescription().getDependencies());
				if(deps.contains(plugin.getDescription().getName())) {
					boolean matched = true;
					
					for(String dep : deps) {
						if(!this.isPluginEnabled(dep)) matched = false;
					}
					
					if(matched) this.enablePlugin(p);
				}
			}
		}
	}
	
	public void disablePlugins() {
		for(Plugin plugin : this.plugins) {
			this.disablePlugin(plugin);
		}
	}
	
	public void disablePlugin(Plugin plugin) {
		if(!plugin.isEnabled()) return;
		
		OpenClassic.getLogger().info(Color.GREEN + "Disabling " + plugin.getDescription().getFullName() + "...");
		
		plugin.setEnabled(false);
		plugin.onDisable();
	}
	
	public void removePlugin(Plugin plugin) {
		this.plugins.remove(plugin);
	}
	
	public void clearPlugins() {
		this.plugins.clear();
	}
	
	public Plugin getPlugin(String name) {
		for(Plugin plugin : this.plugins) {
			if(plugin.getDescription().getName().equalsIgnoreCase(name)) return plugin;
		}
		
		return null;
	}
	
	public List<Plugin> getPlugins() {
		return new ArrayList<Plugin>(this.plugins);
	}
	
	public boolean isPluginEnabled(String name) {
		return this.getPlugin(name) != null;
	}
	
	@SuppressWarnings("unchecked")
	public PluginDescription getDescription(File file) {
		if(file == null) return null;
		
        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                OpenClassic.getLogger().severe("Plugin in file " + file.getName() + " does not contain a plugin.yml!");
                return null;
            }

            Map<String, Object> yml = (Map<String, Object>) (new Yaml()).load(jar.getInputStream(entry));
            
            return new PluginDescription((String) yml.get("name"), (String) yml.get("version"), (String) yml.get("main-class"), (String) yml.get("depends"));
        } catch (Exception e) {
            OpenClassic.getLogger().severe("Failed to load plugin description!");
            e.printStackTrace();
            return null;
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
	}
	
	public Collection<Listener> getListeners() {
		return this.listeners.keySet();
	}
	
	public List<Listener> getListeners(String plugin) {
		List<Listener> result = new ArrayList<Listener>();
		
		for(Listener listener : this.listeners.keySet()) {
			if(this.listeners.get(listener).getDescription().getName().equals(plugin)) {
				result.add(listener);
			}
		}
		
		return result;
	}
	
	public void registerListener(Listener listener, Plugin plugin) {
		this.listeners.put(listener, plugin);
	}
	
}
