# ElderlyAssist

Plenty of people can make a video call but get stuck when they need to make the text on their phone
bigger. The setting is buried a few screens deep, under a name they wouldn't think to look for, so
they end up phoning someone to ask. ElderlyAssist is meant for moments like that.

You tap a help button floating on the screen and say what you want, for example "make the text
bigger". ElderlyAssist then takes you through it one step at a time. It highlights the button you
need to press and reads the instruction out loud. It never taps anything for you. You press every
button yourself, so you learn the way there, and nothing changes without you doing it.

We built it at the JetBrains hackathon at NYU Abu Dhabi in September 2026 and came 3rd out of
about 40 teams.

## What it does

ElderlyAssist runs as an Android accessibility service. That lets it see what's on the screen and
draw on top of any app, whether that's Settings, WhatsApp or something else.

1. **You ask.** Tap the help button and say what you're trying to do, in your own words.
2. **It works out the task.** `VoiceTaskMatcher` matches what you said to a task it knows how to
   help with.
3. **It points.** `GuidanceEngine` goes through the task's steps while watching the screen. For each
   step it finds the right button, highlights it and reads the instruction aloud.
4. **It notices when you're stuck.** If nothing on screen matches the current step for 8 seconds,
   it decides you're stuck. After 15 seconds it decides you're lost, and it asks an LLM to work out
   where you've ended up and how to get you back.

## How it's put together

We wanted to be able to test the guidance logic without a phone. So `GuidanceEngine` is plain
Kotlin in `commonMain`. It has no Android imports and never reads the clock; whoever calls it passes
in the current time. Because of that, every step it takes, including the timed "stuck" and "lost"
ones, can be replayed in an ordinary unit test with no emulator.

Only the parts that truly need Android live in the Android app: the accessibility service, the
overlay, speech recognition and text-to-speech.

All the code lives in the `com.elderlyassist` package.

```
shared/src/commonMain/kotlin/.../guidance/    The engine, matchers and task definitions (plain Kotlin)
shared/src/commonTest/                        Tests for the engine and matchers
androidApp/src/main/kotlin/.../accessibility  Reading the screen
androidApp/src/main/kotlin/.../overlay        The help button and the highlights
androidApp/src/main/kotlin/.../speech         Listening and speaking
androidApp/src/main/kotlin/.../llm            Getting you back on track when you're lost
```

## Trying it yourself

You'll need JDK 17 or newer and the Android SDK.

```bash
./gradlew :androidApp:assembleDebug
```

Install the APK on your phone. Then switch on ElderlyAssist under **Settings → Accessibility** and
allow it to draw over other apps.

The "get me back on track" feature uses an LLM and is optional. To turn it on, add your API keys to
`local.properties`. That file is gitignored, so the keys never get committed:

```properties
llmApiKey=your-anthropic-key
openAiApiKey=your-openai-key
```

Without keys, everything else works as normal. Only the recovery for when you're lost is switched
off.

## Running the tests

```bash
./gradlew :shared:jvmTest
```

There are 16 tests. They cover the guidance steps, matching your "yes" or "no" answers, and
matching what you say to a task.

## Where it's at

This is a hackathon prototype, and it shows in a few places:

- It only knows a small set of tasks, and we wrote each one by hand. It matches against phrasings
  it already knows instead of understanding any request.
- It runs on Android only. The project uses the Kotlin Multiplatform template, so there's a
  `desktopApp/` folder, but there's nothing in it. The overlay depends on Android's accessibility
  APIs, so a desktop version wouldn't make much sense.
- It speaks in your phone's default language. Whether other languages work depends on which
  text-to-speech voices your phone has installed.
