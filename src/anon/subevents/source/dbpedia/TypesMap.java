package anon.subevents.source.dbpedia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import anon.subevents.pipeline.Config;

public class TypesMap {

	private Map<String, Set<String>> types = new HashMap<String, Set<String>>();

	public void load() {
		
		System.out.println("Load TypesMap: Start.");
		
		String fileName = Config.getValue("data_folder") + "types_en.csv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String resource = parts[0];
				String type = parts[1];
				if (!this.types.containsKey(resource))
					this.types.put(resource, new HashSet<String>());
				types.get(resource).add(type);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				it.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Load TypesMap: Done.");
	}

	public Set<String> getTypes(String source) {
		Set<String> target = this.types.get(source.replace(" ", "_"));
		if (target == null)
			return new HashSet<String>();
		else
			return target;
	}

	public Set<String> getResourcesOfType(String type) {
		Set<String> resources = new HashSet<String>();
		for (String resource : this.types.keySet()) {
			if (this.types.get(resource).contains(type))
				resources.add(resource);
		}
		return resources;
	}

}
