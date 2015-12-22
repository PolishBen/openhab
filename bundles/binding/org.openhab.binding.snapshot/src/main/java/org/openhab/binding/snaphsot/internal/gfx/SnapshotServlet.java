package org.openhab.binding.snaphsot.internal.gfx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.snaphsot.SnapshotBindingConfig;
import org.openhab.binding.snaphsot.SnapshotBindingProvider;
import org.openhab.binding.snaphsot.SnapshotContext;
import org.openhab.binding.snaphsot.SnapshotFileManager;
import org.openhab.io.net.http.SecureHttpContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotServlet extends HttpServlet {
	private static final long serialVersionUID = -8254216127563270956L;
	private static final Logger logger = LoggerFactory.getLogger(SnapshotServlet.class);

	private static final String SERVLET_NAME = "/snapshot";
	private static final String RESOURCES = "./webapps/snapshot-resources";
	
	private static final String ACTION_SNAPSHOT = "snapshot";
	private static final String ACTION_LIST_FILES_IN_GROUP = "list";

	private HttpService httpService;

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	public void unsetHttpService(HttpService httpService) {
		this.httpService = null;
	}

	protected void activate() {
		try {
			logger.info("Starting up snapshot servlet at " + SERVLET_NAME);

			Hashtable<String, String> props = new Hashtable<String, String>();
			httpService.registerServlet(SERVLET_NAME, this, props, createHttpContext());

		} catch (Exception ex) {
			logger.error("Error during weather servlet startup", ex);
		}
	}

	protected void deactivate() {
		httpService.unregister(SERVLET_NAME);
	}

	private HttpContext createHttpContext() {
		HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
		return new SecureHttpContext(defaultHttpContext, "openHAB.org");
	}
	
	private Set<String> getItemNames(Collection<SnapshotBindingProvider> providers){
		Set<String> result = new HashSet<String>();
		for(SnapshotBindingProvider p : providers){
			result.addAll(p.getItemNames());
		}	
		return result;
	}	
	
	private void doListFilesAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		File listGroupTemplate = new File(RESOURCES + "/list.html");
		if (!listGroupTemplate.exists()) {
			throw new ServletException("list.html not found at " + RESOURCES);
		}
		
		File fileTemplate = new File(RESOURCES + "/file.html");
		if (!fileTemplate.exists()) {
			throw new ServletException("file.html not found at " + RESOURCES);
		}	
		
		String name = request.getParameter("name");
		if (StringUtils.isBlank(name)) {
			throw new ServletException("Name required, please add parameter name to the request");
		}	
		
		String group = request.getParameter("group");
		if (StringUtils.isBlank(group)) {
			throw new ServletException("Group number required, please add parameter group to the request");
		}	
		
		SnapshotContext context = SnapshotContext.getInstance();
		SnapshotBindingConfig config = getConfig(context, name);
		
		if(config==null){
			logger.warn("No config found for {}", name);
		} else {
			Path directoryPath = config.getPath();
			logger.debug("Found directoryPath: "+directoryPath);
			
			SnapshotFileManager sfm = new SnapshotFileManager(directoryPath);
			List<Path> files = sfm.getFilesInGroup(Integer.valueOf(group));
			Map<String, String> keyValues = new HashMap<String, String>();
			keyValues.put("list", renderFiles(fileTemplate, name, files));
			
			TokenReplacingReader replReader = new TokenReplacingReader(new FileReader(listGroupTemplate), keyValues);	
			IOUtils.copy(replReader, response.getOutputStream());	
		}
	}
	
	private String renderFiles(File fileTemplate, String itemName, List<Path> files) throws IOException{
		StringBuilder result = new StringBuilder();
		for(Path file : files){
			Map<String, String> keyValues = new HashMap<String, String>();
			keyValues.put("itemName", itemName);
			keyValues.put("file", file.getFileName().toString());
			TokenReplacingReader replReader = new TokenReplacingReader(new FileReader(fileTemplate), keyValues);
			StringWriter writer = new StringWriter();
			IOUtils.copy(replReader, writer);
			result.append(writer.toString());
		}
		return result.toString();
	}	
	
	private void doHomePageAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		File indexTemplate = new File(RESOURCES + "/index.html");
		if (!indexTemplate.exists()) {
			throw new ServletException("index.html not found at " + RESOURCES);
		}
		
		File groupTemplate = new File(RESOURCES + "/group.html");
		if (!groupTemplate.exists()) {
			throw new ServletException("group.html not found at " + RESOURCES);
		}		
		
		SnapshotContext context = SnapshotContext.getInstance();
		context.getProviders();
				
		Map<String, String> keyValues = new HashMap<String, String>();
		Set<String> itemNames = getItemNames(context.getProviders());
		for(String itemName : itemNames){
			SnapshotBindingConfig config = getConfig(context, itemName);
			SnapshotFileManager fileManager = new SnapshotFileManager(config.getPath());
			int groupCount = fileManager.getGroupCount();
			keyValues.put(itemName+".fileCount", String.valueOf(fileManager.getFileCount()));	
			keyValues.put(itemName+".groupCount", String.valueOf(groupCount));			
			keyValues.put(itemName+".groups", renderGroups(groupTemplate, itemName, groupCount));
		}
		
		TokenReplacingReader replReader = new TokenReplacingReader(new FileReader(indexTemplate), keyValues);	
		IOUtils.copy(replReader, response.getOutputStream());	
	}
	
	private String renderGroups(File groupTemplate, String itemName, int groupCount) throws IOException{
		StringBuilder result = new StringBuilder();
		for(int i=groupCount-1; i>=0; i--){
			Map<String, String> keyValues = new HashMap<String, String>();
			keyValues.put("itemName", itemName);
			keyValues.put("groupIndex", String.valueOf(i+1));
			TokenReplacingReader replReader = new TokenReplacingReader(new FileReader(groupTemplate), keyValues);
			StringWriter writer = new StringWriter();
			IOUtils.copy(replReader, writer);
			result.append(writer.toString());
		}
		return result.toString();
	}
	
 	private void doSnapshotAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("name");
		if (StringUtils.isBlank(name)) {
			throw new ServletException("Name required, please add parameter name to the request");
		}	
		
		String size = request.getParameter("size");
		int width=0;
		int height=0;
		if (!StringUtils.isBlank(size)) {
			width = Integer.valueOf(size.split(":")[0]);
			height = Integer.valueOf(size.split(":")[1]);
		}			
				
		SnapshotContext context = SnapshotContext.getInstance();
		SnapshotBindingConfig config = getConfig(context, name);
		
		if(config==null){
			logger.warn("No config found for {}", name);
		} else {
		
			Path directoryPath = config.getPath();
			logger.debug("Found directoryPath: "+directoryPath);
			
			SnapshotFileManager sfm = new SnapshotFileManager(directoryPath);
			
			Path file;
			
			String group = request.getParameter("group");
			String fileName = request.getParameter("file");
			
			if (!StringUtils.isBlank(group)) {
				file = sfm.getGroupFile(Integer.valueOf(group));
			} else if(!StringUtils.isBlank(fileName)) {
				file = sfm.getFile(fileName);
			} else {
				file = sfm.getNewestFile();
			}
			
			if(file==null){
				logger.debug("No snapshots found in directory!");
			} else {
				logger.debug("Rendering: "+file.getFileName());
				BufferedImage originalImage = ImageIO.read(file.toFile());
				if(width==0 || height==0){
					ImageIO.write(originalImage, "jpg", response.getOutputStream());
				} else {
				    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
				    BufferedImage resizedImage = resizeImage(originalImage, type, width, height);
					ImageIO.write(resizedImage, "jpg", response.getOutputStream());
				}
			}
		}	
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = request.getParameter("action");

		if(ACTION_SNAPSHOT.equalsIgnoreCase(action)){
			doSnapshotAction(request, response);
		} else if(ACTION_LIST_FILES_IN_GROUP.equalsIgnoreCase(action)) {
			doListFilesAction(request, response);
		} else {
			doHomePageAction(request, response);
		}
	}
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
	    BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
	    Graphics2D g = resizedImage.createGraphics();
	    g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
	    g.dispose();

	    return resizedImage;
	}
	
	private SnapshotBindingConfig getConfig(SnapshotContext context, String itemName){
		logger.debug("{} providers", context.getProviders().size());
		for(SnapshotBindingProvider p : context.getProviders()){
			return p.getBindingFor(itemName);
		}	
		return null;
	}	

}
