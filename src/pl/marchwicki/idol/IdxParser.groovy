package pl.marchwicki.idol

import groovy.text.SimpleTemplateEngine 

final class IdxParser {

	IdxParser(File f) {
		println "Parsing file: " + f;
		
				def fieldTemplate = '\t\t<field name="$name">$value</field>\n';
				
				def engine = new SimpleTemplateEngine();
						
				def newFile = new File(f.absolutePath + ".xml");
				newFile.delete();
				newFile = new File(f.absolutePath + ".xml");
				
				newFile.append "<!-- $f.name -->\n"
				newFile.append "<add>\n";
				
				//TODO: add DRESECTION into multi-valued content fields
				
				def fileContent = f.getText();
				fileContent.eachMatch("#DREREFERENCE (.*)", {
					def id = it[1].replace("\\", "\\\\");
		
					println " * Document found: #DREREFERENCE $id"
					
					newFile.append "\t<!-- #DREREFERENCE $id -->\n"
					newFile.append "\t<doc>\n"
					newFile.append engine.createTemplate(fieldTemplate).make(["name":"id", "value":id]).toString();
					
					fileContent.eachMatch("(?s)#DREREFERENCE $id.*?#DREENDDOC", {
						it.eachLine({
							it.findAll '#DREFIELD (.*?)="(.*?)"', {
								newFile.append engine.createTemplate(fieldTemplate).make(["name":it[1], "value":it[2]]).toString();
								};
							})
						it.find '(?s)#DRECONTENT(.*?)#DRE', {str, match ->
							newFile.append engine.createTemplate(fieldTemplate).make(["name":"content", "value":"<![CDATA["+match+"]]>"]).toString();
							}
						});
					
					newFile.append "\t</doc>\n"
					})
				
				newFile.append "</add>\n";
	}
	
	static void main(def args) {
		if (args.length == 0) {
			printHelp();
			return;
		}

		def filePath = args[0];
		File f = new File(filePath);
		if (f.isDirectory()) {
			f.traverse { new IdxParser(it) }
		} else if (f.isFile()) {
			new IdxParser(f)
		} else {
			printNotAFileError();
			return;
		}
	}
	
	static void printHelp() {
		println("Idol idx to SolrXML parser");
		println("Usage: IdxParser [file | directory]")
	}
	
	static void printNotAFileError() {
		println("The argument is not a valid file or directory");
		println("---------------------");
		printHelp();	
	}
	
}
