# Porting carpet-shadow 1.20.1 -> 1.21.11

Этот форк — стартовая точка, а не готовый к сборке результат. Апстрим
(mattymatty97/carpet-shadow) заархивирован, последняя ветка `1.20` таргетит
Minecraft 1.20.1. Между 1.20.1 и 1.21.11 Mojang несколько раз ломала
внутренности, на которые мод завязан напрямую через миксины, так что часть
файлов гарантированно не соберётся без ручной доработки.

## Что уже сделано в этом форке

- `gradle.properties` / `build.gradle`: minecraft_version, yarn_mappings,
  loader_version, carpet_core_version, Java 21, актуальный Loom.
- `fabric.mod.json`: depends на `minecraft: 1.21.11`, `carpet: >=1.4.194`.
- `carpet-shadow.mixins.json`: compatibilityLevel JAVA_21.
- Добавлен `.github/workflows/build.yml` (в репозитории не было закоммичен
  `gradle/wrapper/gradle-wrapper.properties`, поэтому workflow ставит Gradle
  напрямую через `gradle/actions/setup-gradle`, а не через `./gradlew`; если
  сгенерируешь и закоммитишь wrapper — можно переключить обратно).
- В самых рискованных файлах оставлены `// FIXME [PORT 1.21.11]` с описанием,
  что именно проверить.

## Список проблемных мест (по номерам из комментариев в коде)

### 1. `mixins/persistence/ItemStackMixin.java`
Инжектится в `ItemStack.fromNbt`/`writeNbt`. Начиная с 1.20.5 сериализация
`ItemStack` переписана на систему Data Components + кодеки — старых методов
`fromNbt(NbtCompound)`/`writeNbt(NbtCompound)` в таком виде уже нет. Это ядро
мода: именно здесь "shadow id" пишется/читается при сохранении предмета на
диск. Нужно найти актуальную точку (де)сериализации в декомпилированных
1.21.11-сорцах и переиспользовать логику там же.

### 2. `mixins/tooltip/PacketByteBufMixin.java`
Вручную собирает NBT `display`/`Lore` и суёт туда строку с shadow id, чтобы
её увидел ванильный клиент. Lore теперь — DataComponent
(`List<Text>`), а не NBT-тег, и `ItemStack.getNbt()/setNbt()` в таком виде
не существуют. `writeItemStack`/`readItemStack`, вероятно, больше не гоняют
`NbtCompound` напрямую (сериализация стека идёт через кодек компонентов).
Этот файл придётся переписать логически, а не просто переименовать методы.

### 3. `mixins/tooltip/ItemStackMixin.java`
`ItemStack#getTooltip` менял сигнатуру (появился параметр
tooltip-контекста/типа). Свериться с текущей сигнатурой.

### 4. `mixins/general/LootTableMixin.java`
Таргетится на `method_331` — это intermediary-имя из мэппингов 1.20.1, оно
**не гарантированно** указывает на тот же метод (или вообще существует) в
1.21.11. Нужно заново найти в LootTable метод, копирующий стек с заданным
count при генерации лута.

### 5. `mixins/crafting/RecipeManagerMixin.java`
Инжектится в конкретную форму байткода `RecipeManager#apply` образца 1.20.1
(локальная `Map<RecipeType<?>, ImmutableMap.Builder<...>>`, `@Local(ordinal=...)`).
Загрузка/хранение рецептов несколько раз менялось с тех пор — véroятно,
проще переписать этот хук на другой тип инжекта (например, хук после
регистрации рецептов), чем воспроизводить старую форму локальных переменных.

### 6. `interfaces/InventoryItem.java` + всё в `mixins/inv_updates/loaders/`
Эти миксины делают `@Redirect` на
`BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V`. В более поздних
1.21.x снапшотах Mojang ввела абстракцию `ReadView`/`WriteView` вместо голого
`NbtCompound` во многих местах чтения/записи NBT (могло затронуть и
`BlockEntity`). Нужно проверить актуальную сигнатуру.

### 7. `CarpetShadow.java`
`CarpetExtension` API за 1.4.112 -> 1.4.194 тоже менялся (например,
`customSettingsManager` убрали в пользу `extensionSettingsManager`). Сверить
интерфейс `CarpetExtension` в актуальной версии carpet.

## Файлы, которые скорее всего соберутся почти без изменений

Всё, что работает через `ShadowItem`/`ShifingItem`/`ItemEntitySlot`
интерфейсы, `Globals`, `CarpetShadowSettings`, `RandomString`, и миксины,
которые просто дёргают `copy()`/`isEmpty()`/`setCount()`/`getStack()` и
подобные стабильные методы `ItemStack`/`Inventory`/`Slot`/`ScreenHandler` —
скорее всего перенеслись без проблем (могут потребовать точечных правок
дескрипторов методов в `@At(target = ...)`, если сигнатуры чуть изменились,
но общая логика жива).

## Рекомендуемый порядок действий

1. Запустить `./gradlew genSources` (или собрать через workflow) — Loom
   подтянет decompiled 1.21.11 исходники в `.gradle`/кэш Loom, по ним удобно
   свериться с актуальными именами/сигнатурами.
2. Погнать сборку через GitHub Actions — она упадёт на первых же миксинах.
3. Разбирать ошибки компиляции по одной, начиная с файлов из списка выше.
4. После того как всё компилируется — проверить рантайм-поведение
   (persist/vanish режимы, тултип, дропперы/хопперы, крафт shadow-предмета)
   вживую на сервере, т.к. логика в п.1-5 не просто переименовывается, а
   местами переписывается.
