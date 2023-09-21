import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:path/path.dart' as path;
import 'package:phosphor_flutter/phosphor_flutter.dart';

import "./phosphor.dart" as phosphor;

final String toolsRoot =
    path.normalize(path.join(Directory.current.path, "../../"));

final String resourceFolder = path.join(toolsRoot, "..", 'resources', 'icons');

Future main() async {
  MyIconApp app = MyIconApp(phosphor.icons);
  runApp(app);

  await pumpEventQueue();

  for (phosphor.IconTuple icon in phosphor.icons) {
    await findAndSave(icon.smallKey,
        path.join(resourceFolder, icon.style, '${icon.name}.png'));
    await findAndSave(icon.largeKey,
        path.join(resourceFolder, icon.style, '${icon.name}@2x.png'));
  }

  print('Finished generating icons, quitting...');
  exit(0);
}

class MyIconApp extends StatelessWidget {
  final List<phosphor.IconTuple> icons;

  MyIconApp(this.icons);

  @override
  Widget build(BuildContext context) {
    const color = Color(0xFF777777);
    Stack smallStack = Stack(
      children: icons.map((icon) {
        return RepaintBoundary(
          child: PhosphorIcon(
            icon.data,
            size: 16.0,
            key: icon.smallKey,
            color: color,
          ),
        );
      }).toList(),
    );
    Stack largeStack = Stack(
      children: icons.map((icon) {
        return RepaintBoundary(
          child: PhosphorIcon(
            icon.data,
            size: 32.0,
            key: icon.largeKey,
            color: color,
          ),
        );
      }).toList(),
    );
    return MaterialApp(
      title: 'Flutter Test',
      home: Center(
        child: Column(
          children: [
            Row(
              children: [smallStack],
            ),
            Row(
              children: [largeStack],
            ),
            PhosphorIcon()
          ],
        ),
      ),
    );
  }
}

Future findAndSave(Key key, String path) async {
  Finder finder = find.byKey(key);

  final Iterable<Element> elements = finder.evaluate();
  Element element = elements.first;
  final Future<ui.Image> imageFuture = _captureImage(element);

  final ui.Image image = await imageFuture;
  final ByteData? bytes =
      await (image.toByteData(format: ui.ImageByteFormat.png));

  if (bytes == null) {
    return;
  }
  await File(path)
    ..createSync(recursive: true)
    ..writeAsBytes(bytes.buffer.asUint8List());

  print('wrote $path');
}

Future<ui.Image> _captureImage(Element element) {
  assert(element.renderObject != null);
  RenderObject renderObject = element.renderObject!;
  while (!renderObject.isRepaintBoundary) {
    renderObject = renderObject.parent!;
  }

  assert(!renderObject.debugNeedsPaint);
  final OffsetLayer layer = renderObject.debugLayer! as OffsetLayer;
  return layer.toImage(renderObject.paintBounds);
}
