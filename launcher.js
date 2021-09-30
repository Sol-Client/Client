const https = require("https");
const fs = require("fs");
const path = require("path");
const os = require("os");
const unzipper = require("unzipper");
const childProcess = require("child_process");
const auth = require("./auth");
const yggdrasil = auth.YggdrasilAuthService.instance;
const Patcher = require("./patcher.js");
const axios = require("axios");
const tar = require("tar");

function getOsName() {
	switch(os.type()) {
		case "Linux":
			return "linux";
			break;
		case "Darwin":
			return "osx";
			break;
		case "Windows_NT":
			return "windows";
			break;
	}
}

function getJdkOsName() {
	switch(os.type()) {
		case "Linux":
			return "linux";
			break;
		case "Darwin":
			return "mac";
			break;
		case "Windows_NT":
			return "windows";
			break;
	}
}

class Utils {

	static minecraftDirectory;
	static legacyDirectory;
	static librariesDirectory;
	static versionsDirectory;
	static assetsDirectory;
	static assetObjectsDirectory;
	static gameDirectory;
	static version = "beta-3.0";

	static getMinecraftDirectory() {
		Utils.minecraftDirectory = os.homedir();
		Utils.legacyDirectory = os.homedir();
		switch (getOsName()) {
			case "linux":
				Utils.minecraftDirectory += "/.config/SolClient";
				Utils.legacyDirectory += "/.config/parrotclient";
				break;
			case "osx":
				Utils.minecraftDirectory += "/Library/Application Support/SolClient";
				Utils.legacyDirectory += "/Library/Application Support/parrotclient";
				break;
			case "windows":
				Utils.minecraftDirectory += "/AppData/Roaming/SolClient";
				Utils.legacyDirectory += "/AppData/Roaming/parrotclient";
				break;
		}

		if(fs.existsSync(Utils.legacyDirectory) && !fs.existsSync(Utils.minecraftDirectory)) {
			fs.renameSync(Utils.legacyDirectory, Utils.minecraftDirectory);
			fs.unlinkSync(Utils.minecraftDirectory + "/account.json");
		}

		Utils.librariesDirectory = Utils.minecraftDirectory + "/libraries";
		Utils.versionsDirectory = Utils.minecraftDirectory + "/versions";
		Utils.assetsDirectory = Utils.minecraftDirectory + "/assets";
		Utils.assetObjectsDirectory = Utils.assetsDirectory + "/objects";
		Utils.accountFile = Utils.minecraftDirectory + "/account.json";
		Utils.gameDirectory = Utils.minecraftDirectory + "/minecraft";
	}

	static isAlreadyDownloaded(file, size) {
		return fs.existsSync(file) && fs.statSync(file).size == size;
	}

	static download(url, file, size) {
		if(!fs.existsSync(path.dirname(file))) {
			fs.mkdirSync(path.dirname(file), { recursive: true });
		}
		if(!Utils.isAlreadyDownloaded(file, size)) {
			return new Promise((resolve) => {
				https.get(url, async(response) => {
					if(response.code == 404) {
						resolve(false);
					}
					if(response.headers.location) {
						var result = await Utils.download(response.headers.location, file, size);
						resolve(result);
						return;
					}
					response.pipe(fs.createWriteStream(file));
					response.on("end", () => {
						resolve(true);
					});
				});
			});
		}
		return new Promise((resolve) => resolve(true));
	}

}

Utils.getMinecraftDirectory();

class Launcher {

	static instance = new Launcher();
	account = null;

	launch(callback) {
		Manifest.getManifest((manifest) => {
			Manifest.getVersion(manifest, "1.8.9", async(version) => {
				version.id = "SolClient-" + Utils.version + "-" + version.id;
				var jars = [];
				var versionFolder = Version.getPath(version);
				var versionJar = Version.getJar(version);
				var nativesFolder = Version.getNatives(version);
				var optifineRelative = "net/optifine/optifine/1.8.9_HD_U_M5/optifine-1.8.9_HD_U_M5.jar";
				var optifine = Utils.librariesDirectory + "/" + optifineRelative;
				version.libraries.push({
					downloads: {
						artifact: {
							url: "https://repo.maven.apache.org/maven2/org/slick2d/slick2d-core/1.0.2/slick2d-core-1.0.2.jar",
							path: "org/slick2d/slick2d-core/1.0.2/slick2d-core-1.0.2.jar",
							size: 590652
						}
					}
				});
				version.libraries.push({
					downloads: {
						artifact: {
							url: "https://repo.codemc.io/repository/maven-public/com/logisticscraft/occlusionculling/0.0.5-SNAPSHOT/occlusionculling-0.0.5-20210620.172315-1.jar",
							path: "com/logisticscraft/occlusionculling/0.0.5-SNAPSHOT/occlusionculling-0.0.5-20210620.172315-1.jar",
							size: 12926
						},
					}
				});
				version.libraries.push({
					downloads: {
						artifact: {
							url: "https://repo.hypixel.net/repository/Hypixel/net/hypixel/hypixel-api-core/4.0/hypixel-api-core-4.0.jar",
							path: "net/hypixel/hypixel-api-core/4.0/hypixel-api-core-4.0.jar",
							size: 76463
						}
					}
				});
				for(var library of version.libraries) {
					if(!Library.isApplicable(library.rules)) {
						continue;
					}
					if(library.downloads.artifact != null) {
						await Library.download(library.downloads.artifact);
						jars.push(Library.getPath(library.downloads.artifact));
					}
					if(library.natives != null) {
						var nativeName = library.natives[getOsName()];
						if(nativeName != null) {
							var download = library.downloads.classifiers[nativeName];
							if(download != null) {
								await Library.download(download);
								var zip = fs.createReadStream(Library.getPath(download))
									.pipe(unzipper.Parse({ forceStream: true }));
								for await(const entry of zip) {
									const fileName = entry.path;

									if(library.extract.exclude != null && library.extract.exclude.includes(fileName)) {
										await entry.autodrain();
									}
									else {
										var destination = nativesFolder + "/" + fileName;
										if(!fs.existsSync(path.dirname(destination))) {
											fs.mkdirSync(path.dirname(destination), { recursive: true });
										}
	 									await entry.pipe(fs.createWriteStream(nativesFolder + "/" + fileName));
									}
								}
							}
						}
					}
				}

				var optifineSize = 2585014;

				if(!Utils.isAlreadyDownloaded(optifine, optifineSize)) {
					await Library.download({
							url: await Patcher.getOptiFine(),
							size: optifineSize,
							path: optifineRelative
						});
				}

				for(var object of Object.values((await Version.getAssetIndex(version)).objects)) {
					await AssetIndex.download(object);
				}

				await Version.downloadJar(version);

				var java;

				await new Promise((resolve) => {
					axios.get("https://api.adoptopenjdk.net/v3/assets/feature_releases/8/ga" +
							"?release_type=ga" +
							`&architecture=${os.arch()}` +
							"&heap_size=normal" +
							"&image_type=jre" +
							"&jvm_impl=hotspot" +
							`&os=${getJdkOsName()}` +
							"&page=0" +
							"&page_size=1" +
							"&project=jdk" +
							"&sort_method=DATE" +
							"&sort_order=DESC" +
							"&vendor=adoptopenjdk")
						.then(async(response) => {
							var jrePackage = response.data[0].binaries[0].package;
							var name = jrePackage.name;
							var path = Utils.minecraftDirectory + "/jre/" + name;
							var dest = Utils.minecraftDirectory + "/jre/"
									+ name.substring(0, name.indexOf("."));
							var doneFile = dest + "/.done";
							if(!fs.existsSync(dest + "/.done")) {
								await Utils.download(jrePackage.link,
									path, jrePackage.size);


								if(!fs.existsSync(dest)) {
									fs.mkdirSync(dest, {recursive: true});
								}

								if(name.endsWith(".tar.gz")) {
									await tar.x({
										file: path,
										C: dest
									});
								}
								else if(name.endsWith(".zip")) {
									await fs.createReadStream(path).pipe(unzipper.Extract({path: dest}));
								}

								fs.closeSync(fs.openSync(doneFile, "w"));

								fs.unlinkSync(path);
							}

							java = dest + "/" + response.data[0].release_name
									+ "-jre/bin/java" + (getOsName() == "windows" ? ".exe" : "");

							resolve();
						});
				});

				var args = [];
				args.push("-Djava.library.path=" + nativesFolder);
				args.push("-Dme.mcblueparrot.client.version=" + Utils.version);

				var classpathSeparator = getOsName() == "windows" ? ";" : ":";
				var classpath = "";

				args.push("-cp");

				for(var jar of jars) {
					classpath += jar;
					classpath += classpathSeparator;
				}

				classpath += await Patcher.patch(java, Version.getJar(version),
						optifine, Version.getPath(version) + "/" + version.id +
						"-patched.jar", classpathSeparator);

				args.push(classpath);

				args.push("net.minecraft.client.main.Main");

				args.push("--version");
				args.push("SolClient");

				args.push("--username");
				args.push(this.account.username);

				args.push("--uuid");
				args.push(this.account.uuid);

				args.push("--accessToken");
				args.push(this.account.accessToken);

				args.push("--userType");
				args.push("mojang");

				args.push("--versionType");
				args.push("release");

				if(this.account.demo) {
					args.push("--demo");
				}

				args.push("--assetsDir");
				args.push(Utils.assetsDirectory);

				args.push("--assetIndex");
				args.push(version.assetIndex.id);

				args.push("--gameDir");
				args.push(Utils.gameDirectory);

				var process = childProcess.spawn(java, args, { cwd: Utils.minecraftDirectory });

				process.stdout.on("data", (data) => {}); // Don't know why you need this.

				callback();
			});
		});
	}

}

class Manifest {

	static #instance;

	static getManifest(callback) {
		if(Manifest.#instance != null) {
			callback(Manifest.#instance);
			return;
		}
		https.get("https://launchermeta.mojang.com/mc/game/version_manifest.json",
				(response) => {
			var body = "";
			response.on("data", (data) => {
				body += data;
			});
			response.on("end", () => {
				callback(Manifest.#instance = JSON.parse(body));
			});
		});
	}

	static getVersion(manifest, id, callback) {
		for(var version of manifest.versions) {
			if(version.id == id) {
				https.get(version.url, (response) => {
					var body = "";
					response.on("data", (data) => {
						body += data;
					});
					response.on("end", () => {
						callback(JSON.parse(body));
					});
				});
				return;
			}
		}
		callback(null);
	}

}

class Version {

	static getAssetIndex(version) {
		return new Promise((resolve) => {
			https.get(version.assetIndex.url, (response) => {
				if(response.code == 404) {
					resolve(null);
				}
				var body = "";
				response.on("data", (data) => {
					body += data;
				});
				response.on("end", () => {
					resolve(JSON.parse(body));
				});
			});
		});
	}

	static getPath(version) {
		return Utils.versionsDirectory + "/" + version.id;
	}

	static getJar(version) {
		return Version.getPath(version) + "/" + version.id + ".jar";
	}

	static getNatives(version) {
		return Version.getPath(version) + "/" + version.id + "-natives";
	}

	static downloadJar(version) {
		return Utils.download(version.downloads.client.url, Version.getJar(version), version.downloads.client.size);
	}

}

class Library {

	static getPath(download) {
		return Utils.librariesDirectory + "/" + download.path;
	}

	static isApplicable(rules) {
		if(rules == null || rules.length == 0) {
			return true;
		}

		var result = false;
		for(var rule of rules) {
			if(rule.os != null) {
				if(rule.os.name == getOsName()) {
					return rule.action == "allow";
				}
			}
			else {
				result = rule.action == "allow";
			}
		}
		return result;
	}

	static download(download) {
		return Utils.download(download.url, Library.getPath(download), download.size);
	}

}

class AssetIndex {

	static getBasePath(object) {
		return object.hash.substring(0, 2) + "/" + object.hash;
	}

	static getFilePath(object) {
		return Utils.assetObjectsDirectory + "/" + AssetIndex.getBasePath(object);
	}

	static getUrl(object) {
		return "http://resources.download.minecraft.net/" + AssetIndex.getBasePath(object);
	}

	static download(object) {
		return Utils.download(AssetIndex.getUrl(object), AssetIndex.getFilePath(object), object.size);
	}

}

exports.Launcher = Launcher;
exports.Utils = Utils;
