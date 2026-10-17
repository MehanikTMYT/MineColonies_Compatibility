# Включение подготовленного FD-модуля

В обычный JAR эти исходники не входят. В этом окружении уже найден неофициальный FD
26.1.2 (см. FD_API_COMPATIBILITY.md); компиляция против него не заменяет игровой QA.

1. Перенесите коммит расширения MineColonies EMI и коммит Compatibility. Соберите MineColonies.
2. Проверьте настоящий FD JAR:
   `./gradlew compileFdJava -PfdJar=/absolute/path/FD.jar -PminecoloniesJar=/absolute/path/minecolonies.jar`.
   Не создавайте заглушки отсутствующих классов. Для другого порта повторите проверку сигнатур.
3. В отдельной ветке перенесите только файлы из
   `src/disabled/java/steve_gall/minecolonies_compatibility/module/common/farmersdelight/`
   и `module/client/farmersdelight/` в соответствующие каталоги `src/main/java`.
   Подкаталог `client/farmersdelight/jei` оставьте отключённым.
4. В ModuleManager замените комментарий об отключённом farmersdelight этой регистрацией:

   ```java
   public static final OptionalModule<steve_gall.minecolonies_compatibility.module.common.farmersdelight.FarmersDelightModule> FARMERSDELIGHT =
       register("farmersdelight",
           () -> steve_gall.minecolonies_compatibility.module.common.farmersdelight.FarmersDelightModule::new);
   ```

   Это штатная двойная ленивая фабрика Compatibility: FD-классы не инициализируются,
   когда мод отсутствует. Не регистрируйте модуль вторым независимым обработчиком.
   В neoforge.mods.toml добавьте optional-зависимость farmersdelight с ordering AFTER,
   BOTH и диапазоном версии проверенного порта.
5. Соберите обычный JAR с `-PfdJar=...`. Этот параметр предоставляет compileOnly API;
   FD не встраивается в JAR. Для игрового запуска установите настоящий FD отдельно.
6. Проверьте запуск с FD и без FD, сервер и клиент. В колонии проверьте cooking
   (1–6 ингредиентов, контейнер, компоненты), cutting (инструменты и шанс результата),
   plating (порции и контейнеры), перезагрузку рецептов и обучение.
   Новый EMI-плагин регистрирует только обработчики переноса, без повторных категорий.
   Синхронизация cooking/cutting уже добавлена в FarmersDelightModule.

Cultural Delights и Fruits Delight **не включать вместе с базовым FD автоматически**.
Для каждого сначала нужен собственный JAR 26.1.2 и его обязательные зависимости,
компиляция и игровой тест. Проверка наличия одного farmersdelight недостаточна.
Их исходники остаются BLOCKED; простая замена Identifier не означает завершённую адаптацию.

Не меняйте количества порций, шансы, требования инструментов/зданий или профессии
для обхода ошибок API. Это не часть данной адаптации.
