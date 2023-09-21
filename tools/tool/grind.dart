import 'package:grinder/grinder.dart';

main(args) => grind(args);

@Task()
test() => TestRunner().testAsync();

@DefaultTask()
@Depends(icons)
void generate() {}

@Task('Generate Phosphor icons')
Future<void> icons() async {
  await Dart.runAsync('tool/icons/update_icons.dart');
}
