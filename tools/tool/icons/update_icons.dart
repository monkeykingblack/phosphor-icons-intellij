import 'dart:io';
import 'dart:convert';

import 'package:grinder/grinder.dart';
import 'package:path/path.dart' as path;

import '../common.dart';

final String outputFolder = path.join('..', 'resources', 'icons');
final String iconGeneratorLib = path.join('tool', 'icons_generator', 'lib');

Future<void> main() async {
  final String phosphorVersion = getPhosphorVersion();
  final String phostPhorPath = path.join(
      pubCachePath, 'hosted', 'pub.dev', 'phosphor_flutter-$phosphorVersion');

  if (!Directory(phostPhorPath).existsSync()) {
    throw 'Run flutter pub get before running command';
  }

  final String srcPath = path.join(phostPhorPath, 'lib', 'src');

  final String allIconsPath = path.join(srcPath, 'phosphor_icons.dart');

  final List<String> iconStyles =
      parseStyleIconData(File(allIconsPath).readAsStringSync());

  List<Icon> iconList = [];
  for (var style in iconStyles) {
    final String stylePath = path.join(srcPath, style);
    List<Icon> icons = parseIconData(File(stylePath).readAsStringSync());
    generateProperties(icons, '${icons[0].style}.properties', icons[0].style);
    iconList.addAll(icons);
  }

  generateDart(iconList, 'phosphor.dart');

  await generateIcons(path.join('tool', 'icons_generator'));
}

List<String> parseStyleIconData(String data) {
  final RegExp regexp = RegExp(r"'package:phosphor_flutter/src/(\S+)';");
  return regexp.allMatches(data).map((Match match) {
    return match[1]!;
  }).toList();
}

List<Icon> parseIconData(String data) {
  final RegExp regexp = RegExp(
      r"final (\S+) = (?:PhosphorFlatIconData\(0x(\S+), '(\S+)'|PhosphorDuotoneIconData\(\r\s+0x(\S+),\r\s+PhosphorIconData\(0x\S+, '(\S+)')");
  return regexp.allMatches(data).map((Match match) {
    return Icon(
        match.group(1)!,
        int.parse(match.group(2) != null ? match.group(2)! : match.group(4)!,
            radix: 16),
        (match.group(3) != null ? match.group(3)! : match.group(5)!)
            .toLowerCase());
  }).toList();
}

void generateProperties(List<Icon> icons, String filename, String pathSegment) {
  final StringBuffer buf = StringBuffer();
  buf.writeln('# Generated file - do not edit.');
  buf.writeln();
  buf.writeln('# suppress inspection "UnusedProperty" for whole file');

  final Set<int> set = <int>{};
  for (final Icon icon in icons) {
    buf.writeln();
    if (set.contains(icon.codepoint)) {
      buf.write('# ');
    }

    buf.writeln('${icon.codepoint.toRadixString(16)}.codepoint=${icon.name}');
    buf.writeln('${icon.name}=$pathSegment/${icon.name}.png');

    set.add(icon.codepoint);
  }

  final dest = path.join(outputFolder, filename);
  File(dest).writeAsStringSync(buf.toString());
  print('wrote $dest');
}

Future<void> generateIcons(String appFolder) async {
  final Process proc = await Process.start(
      flutterPath, <String>["run", '-d', 'flutter-tester'],
      workingDirectory: appFolder);

  bool hasError = false;
  final errorText = StringBuffer();
  proc.stdout
      .transform(utf8.decoder)
      .transform(const LineSplitter())
      .listen((String line) {
    if (line.contains('ERROR:')) {
      errorText.writeln(line);
      hasError = true;
    }
    stdout.writeln(line);
  });
  proc.stderr
      .transform(utf8.decoder)
      .transform(const LineSplitter())
      .listen((String line) {
    hasError = true;
    errorText.writeln(line);
    stderr.writeln(line);
  });
  final int exitCode = await proc.exitCode;
  if (exitCode != 0 || hasError) {
    throw 'Process exited with error ($exitCode): $errorText';
  }
}

void generateDart(List<Icon> icons, String fileName) {
  final StringBuffer buf = StringBuffer();
  buf.writeln('''
  // Generated file - do not edit.

  import 'package:flutter/widgets.dart';
  import 'package:phosphor_flutter/phosphor_flutter.dart';

  class IconTuple {
    final PhosphorIconData data;
    final String name;
    final String style;
    final Key smallKey = new UniqueKey();
    final Key largeKey = new UniqueKey();
  
    IconTuple(this.data, this.name, this.style);
  }
  
  final List<IconTuple> icons = [''');

  for (final Icon icon in icons) {
    buf.writeln(
        " new IconTuple(PhosphorIcons.${icon.style}.${icon.name}, '${icon.name}', '${icon.style}'),");
  }

  buf.writeln('];');

  final dest = path.join(iconGeneratorLib, fileName);
  File(dest).writeAsStringSync(buf.toString());
  print('wrote $dest');
}

class Icon {
  Icon(this.name, this.codepoint, this.style);

  final String name;
  final int codepoint;
  final String style;

  @override
  String toString() => '$style.$name 0x${codepoint.toRadixString(16)}';
}
