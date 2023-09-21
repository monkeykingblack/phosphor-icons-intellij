import 'dart:io';
import 'dart:convert';

import 'package:path/path.dart' as path;

final String flutterSdkPath = _getFlutterSdkPath();

final String flutterPath = path.join(flutterSdkPath,
    path.join('bin', Platform.isWindows ? 'flutter.bat' : 'flutter'));

final String pubCachePath = _getPubCachePath();

String _getFlutterSdkPath() {
  if (!Platform.resolvedExecutable
      .contains(path.join('bin', 'cache', 'dart-sdk'))) {
    throw 'Please run this script from the version of dart in the Flutter SDK.';
  }

  return path.dirname(path.dirname(
      path.dirname(path.dirname(path.dirname(Platform.resolvedExecutable)))));
}

String _getPubCachePath() {
  final String? pathInEnv = Platform.environment['PUB_CACHE'];
  if (pathInEnv != null) {
    return pathInEnv;
  }

  if (Platform.isWindows) {
    return path.join(Platform.environment['%LOCALAPPDATA%']!, "Pub", "Cache");
  }

  return path.join(Platform.environment['HOME']!, '.pub-cache');
}

Future<void> flutterRun(String script) async {
  final Process proc = await Process.start(
    flutterPath,
    <String>['run', '-d', 'flutter-tester', '-t', script],
  );
  proc.stdout
      .transform(utf8.decoder)
      .transform(const LineSplitter())
      .listen(stdout.writeln);
  proc.stderr
      .transform(utf8.decoder)
      .transform(const LineSplitter())
      .listen(stderr.writeln);
  final int exitCode = await proc.exitCode;
  if (exitCode != 0) {
    throw 'Process exited with code $exitCode';
  }
}

String getPhosphorVersion() {
  final ProcessResult result =
      Process.runSync(flutterPath, <String>["pub", "deps", "--json"]);

  if (result.exitCode != 0) {
    throw 'Error from get version of phosphor icons';
  }

  final Map<String, dynamic> mapResult =
      jsonDecode(result.stdout.toString().trim());

  if (mapResult['packages'] == null ||
      mapResult['packages'] is! List ||
      mapResult.isEmpty) {
    throw 'Error no packages';
  }

  Map<String, dynamic> phosphorDep = (mapResult['packages'] as List)
      .firstWhere((element) => element['name'] == 'phosphor_flutter');

  return phosphorDep['version']! as String;
}
