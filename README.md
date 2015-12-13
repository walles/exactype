# Exactype

![Travis Build Status](https://api.travis-ci.org/walles/exactype.svg?branch=master)

Exactype is an Android keyboard focused on helping the user to type the right thing from the start
rather than correcting the user's mistakes.

Features (will) include:

* Large keys that are easy to hit
* Instant feedback on each key press on whether or not it was precise
* Gestures for common operations (including typing space)

## Hacking
* `git clone git@github.com:walles/exactype.git`
* [Download and install Android Studio](https://developer.android.com/sdk/index.html)
* Start Android Studio
* "Open an existing Android Studio project" and point it to where you cloned the source code
* Next to the green play button in the top toolbar, make sure the dropdown says "Exactype"
* Click the green play button to build / launch, install any requested components
* Do `./gradlew check` to test and lint code before making a pull request. This is
[what Travis does](https://github.com/walles/exactype/blob/master/.travis.yml) anyway, so this is
also a good way of researching Travis problems locally.

Note that the shared Exactype run configuration will first run the unit tests, then launch the app.
To run only the unit tests, start the Unit Tests launch configuration.

## Icon
The `ic_launcher.png` icon is generated from [icon.blend](https://github.com/walles/exactype/blob/master/app/graphics/icon.blend).

To work with the icon, [download the latest version of Blender](http://blender.org/download).

## Releasing
1. Do ```git tag``` and think about what the next version number should be.
2. Do ```git tag --annotate version-1.2.3``` to set the next version number.
3. ```env JAVA_OPTS=-Xmx512m ./gradlew --no-daemon build```
4. Upload ```app/build/outputs/apk/app-release.apk``` to Google Play
5. ```git push --tags```

## TODO Misc
* Add support for an English keyboard layout. This is a step towards emoji support (see below); it
requires us to support more than one keyboard, without needing to make an entirely new keyboard.
* Maybe or maybe not before the emojis, make a cursor-keys keyboard that shows up if you move the
cursor by clicking somewhere. It should contain left, right, backspace and Abc (back to standard
keyboard). My hypothesis is that simplifying cursor positioning would somewhat alleviate the need
for auto correction.

## TODO Prioritized Beta User Issues
* Add emoji support

## TODO for Emoji Support
* Add a keyboard selector that on left swipe scrolls the keyboard sideways. A horizontal scroll view
of some kind comes to mind.
* OK: Make the keyboard selector default to just letting all touch events through.
* OK: Make our GestureDetector detect the start of a left swipe.
* On start left swipe, have the keyboard tell the keyboard selector to take over all touch events.
* Have the keyboard selector start passing touch events through again after the scrolling plus any
resulting animations are done.
* Add an EmojiView. It should be sized like the keyboard.
* When swiping the keyboard away, swipe the EmojiView in.
* When swiping the EmojiView left, swipe the keyboard back in.
* Try how the swiping and the views work when rotating the device.
* Make the EmojiView display something.
* Make the EmojiView display something that is higher than the height of the EmojiView.
* Make the EmojiView scroll vertically on single finger scroll.
* Make the EmojiView display some actual emoji.
* Make the EmojiView display all emoji.
* Make clicking an emoji add it to the input field.

## TODO Before Getting the First Remote Beta Tester
A remote beta tester is somebody I'm not in daily contact with and who will have to fend for herself
/ himself.

* Add an Activity that helps users enabling / choosing the new keyboard.
* Aim for putting popup keyboard's bottom at the user's finger, and horizontally centered around
that finger. Adjust position so that no part of the popup window is off screen.
* Add an emoji keyboard. Switch layouts by swiping left.
* Make the SHIFT key font smaller than the rest of the keyboard
* Make the Backspace key look like a Backspace key
* Hint user about the Action swipe down unless (s)he has already practiced it
* Hint user to swipe right for space unless (s)he has already done that
* Hint user to swipe right twice for . unless (s)he has already done that
* Hint user to swipe right three times for ... unless (s)he has already done that
* Hint user to long press for numbers unless (s)he has already done that
* Hint user to press even longer for alternative letters unless (s)he has already done that
* Hint user to hold shift for numlock unless (s)he has already done that

## TODO Before Publishing on Google Play
* Get FeedbackWindow working with popup keyboard
* Test an English layout and see how that looks; it has shorter rows than Swedish and could look
bad.
* Change keyboard layout by swiping left or right with two fingers.
* Make the letters look nicer by outlining them, shadowing them, anti aliasing them, bolding them,
switching font or something

## Editing ideas
When mistyping something, placing the cursor properly for correcting it feels too hard. How about
this?

* When the user places the cursor in a non-empty field, pop up an arrow keys keyboard somewhere
around the cursor.
* The arrows keyboard should have keys for left, right and hide.
* The arrows keyboard should have a cartoon bubble like pointer to where the cursor was placed so
that it's obvious they belong together.
* It should be possible to drag the arrows keyboard around on screen.
* Typing something on the ordinary keyboard should hide the arrows keyboard.
* Hiding the ordinary keyboard should hide the arrows keyboard.
* Test on a phone in portrait orientation.
* Test on a phone in landscape orientation.
* Test on a tablet in portrait orientation.
* Test on a tablet in landscape orientation.

## Feedback ideas
* Add audio clicks on key releases?
* Condition audio / vibration feedback on how close the hit was to the key we think the user
pressed. Maybe vibrate on down for close keypresses and always audio-pop on up?
* Sound instead of vibrations on non-vibrating hardware?
* Show a marker on the keyboard where the user last hit?
* If the user is too off in her press, neither buzz nor add a key. We need some kind of feedback
though to tell the user that "I know you pressed, but it was too bad a hit so you need to improve".
Maybe flash the keyboard red as well and put a marker where the user pressed?
* Implement a Spell Checker Service. When the user ends a word containing one or more dodgy
keypresses, add the original word + corrections for the dodgy keypresses to the (in memory only)
word list. Maintain a list of at most 50 corrections or so, and possibly drop the whole thing when
the user switches input fields.

## User Feedback
* "I prefer having all keys the same size"
* "I prefer a slanted layout, maybe we'd get that by having all keys the same size?"

## TODO Misc
* Are there cases where we should explicitly close ourselves? In onUnbindInput() for example?
* Add more keyboard layouts.
* Make sure the popup keyboard frame has the same thickness on all sides of the popup keyboard.
* Make sure the hitpoint of lower case 'o' is in the middle of the circle
* Remove all memory allocations from onDraw() and onMeasure() code paths. This is for avoiding GC
pauses during drawing operations.
* Think about keyboard transparency, especially on phones in landscape mode
* Think about sending accessibility events
* Enable users to change keyboard color
* Fix layout bounces, we do four layouts currently on Johan's phone
* Write a test that goes through all keyboard layouts and ensures all layouts can type all glyphs
available in Android's standard font.
* Think about the vertical spacing of the keys; there seems to be more distance between adjacent
lines than between the lines and the edges. Should this be adjusted?
* Move popup keyboard code from Exactype into its own class.
* Implicit numlock on typing first digit?
* Limit keyboard height in millimeters. Try out a good upper bound on a 10 inch tablet.
* On phone / landscape, maybe compress keyboard height to two lines by shifting the middle row to
the side? This would require the same spacing on all rows.
* Think about how much a user should need to swipe at the minimum to make a space; we want to avoid
people making spaces by mistake when hitting buttons sloppily.
* Pressing SHIFT at the middle / end of words should work as in SwiftKey; it should primarily
modify the casing of the current word, but if you keep typing it only affects the upcoming letters?
* Make sure two SHIFT presses means ALL CAPS. Both when starting with lowercase letters and when
being automatically shifted?
* Commit things to the view in the background? The receiving view is sometimes slow, and feeding the
InputConnection in the background should make our keyboard feel snappy even when this happens.
* Automatic caps after '.' on caps-mode-sentence fields. Or always when typing text?
But not in numbers.
* Swipe right after a space => ". "?
* Swipe right after ". " => "... "?
* Auto space after punctuation?
* No auto space after punctuation inside numbers.
* For tablets, consider splitting the keyboard when wide enough
* Make sure popup keyboard is ignored if finger is released outside of it
* Make sure long press is canceled if finger is released outside of keyboard
* Make sure swipe is canceled if finger is released outside of keyboard
* Make sure the vibration duration pref slider doesn't disappear if the user rotates the device
while the slider is showing

## Won't do
* Take keyboard layouts from the AOSP keyboard. I looked into this and it was much too complicated.
Plan B is to create my own layouts and try them on various beta testers.

## DONE
* Create an IME that when selected shows an empty view
* Add licensing information (same as AOSP)
* Draw a Swedish keyboard in the view
* Size the keyboard and its keys properly and automatically
* Find out why we have padding around the view and get rid of it
* Insert characters in the text when the user presses the keys
* Swipe right => insert a space
* Size the letters properly
* Implement a working backspace key
* Treat double taps as two single taps
* Implement a working SHIFT key
* Improve double tap handling; just have the GestureListener deliver two distinct tap events rather
than detecting a double tap and pretending it was two single taps.
* Pressing two different keys in rapid succession must insert both. Currently doing that inserts
neither; most likely because of double tap detection messing things up.
* Make tapping Backspace with text selected remove the selected text
* Automatically start out in the correct Shift mode
* Make the keyboard not resize when pressing SHIFT
* Make long pressing show the numeric keyboard for the duration of the long press
* Make keyboard a bit higher
* Swipe down for "action" operation to the keyboard.
* Insert newline when swiping down when composing a mail in Inbox.
* "Hitting "ä" is too hard, maybe it's too close to the right edge?"
* Make long pressing "a" and keeping still show alternative "a"s like "@" after first showing the
numeric keyboard for a while
* Add long long press support to some key at the right of the keyboard and make sure the popup gets
a reasonable position.
* Make long pressing SHIFT lock the numeric keyboard in place
* In all keyboard modes, put Backspace bottom right
* In all keyboard modes, put mode switch (shift, Shift, NumLock) bottom left
* On both uppercase and lowercase keyboards, clicking mode switch once should switch case, clicking
twice should numlock
* Log the time taken by each text-modifying operation, make sure not to log parts of passwords
* Flash an image of where on the keyboard the user hit above the keyboard on every tap. Create a
`PopupWindow` for this, call `setClippingEnabled(false)` on it and put it wherever you want. To
find out where to position it, you may (or may not) have to call `getLocationInWindow()` /
`getLocationOnScreen()` on the `ExactypeView` first.
* Fade FeedbackWindow out on release rather than just disappearing?
* Add vibrations on key presses
* When sliding around the numeric or a popup keyboard, show what's under your finger in a small
(fixed in place) frame above the keyboard.
* In landscape mode, show actual screen contents rather than just a text box to type in
* Add a .travis.yml build config running the unit tests
* Get the FeedbackWindow working in landscape mode.
* Think about which versions of Android we should build for / support
* Come up with a way of doing code inspections in Travis
* Try rotating the phone 90 degrees while long pressing and verify FeedbackWindow shows the right
thing
* Try rotating the phone 90 degrees while a popup keyboard is showing and verify nothing bad
happens
* Publish beta to Android Market
* Start out with numeric keyboard if the input field is tagged as being numeric.
* Fulfill the "Designed for Tablets" criteria on Google Play Store
* Test on a tablet, at least a simulated one, in both landscape and portrait mode.
* Make holding down backspace work as expected
* Make sure swiping down after editing event time in the Galaxy Alpha calendar hides the keyboard.
* Disable on-tap popup; none of the testers like it.
* Holding backspace first deletes *two* words, then one at a time. It should do one-at-a-time from
the start.
* Add Settings activity with a slider for vibration duration
* Repeating backspace removes one char too many
* On multi touch, interpret the second touch as the end of the first and the start of another single
touch gesture
* Make keyboard more responsive when communicating with the input field is slow.
