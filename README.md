# Exactype

Exactype is an Android keyboard focused on helping the user to type the right thing from the start
rather than correcting the user's mistakes.

Features (will) include:
* Large keys that are easy to hit
* Instant feedback on each key press on whether or not it was precise
* Gestures for common operations (including typing space)

# TODO Before Johan can use Exactype as his Sole IME
* Make long pressing show the numeric keyboard for the duration of the long press
* Make long pressing "a" and keeping still show alternative "a"s like "@" after first showing the
numeric keyboard for a while

# TODO Before Getting the First Beta Tester
* Add a .travis.yml build config running the unit tests
* Make the keyboard not resize when pressing SHIFT
* Make the SHIFT key look like a SHIFT key
* Pressing SHIFT at the middle / end of words should work as in SwiftKey; it should primarily
modify the casing of the current word, but if you keep typing it only affects the upcoming letters.
* Make the Backspace key look like a Backspace key
* Swipe right after a space => ". "
* Swipe right after ". " => "... "
* Make sure two SHIFT presses means ALL CAPS. Both when starting with lowercase letters and when
being automatically shifted.
* Make holding down backspace work as expected
* Test on different resolutions / screen sizes / screen rotations.
* Make long pressing SHIFT lock the numeric keyboard in place
* Automatic SHIFT handling while typing; caps after '.' for example on caps-mode-sentence fields.
* When sliding around the numeric keyboard, show what's under your finger in a small (fixed in
place) frame above the keyboard
* Flash an image of where on the keyboard the user hit above the keyboard on every tap
* Add audio clicks on key presses
* Add vibrations on key presses
* Condition audio / vibration feedback on how close the hit was to the key we think the user
pressed.
* Hint user to swipe right for space unless (s)he has already done that
* Hint user to swipe right twice for . unless (s)he has already done that
* Hint user to swipe right three times for ... unless (s)he has already done that
* Hint user to long press for numbers unless (s)he has already done that
* Hint user to press even longer for alternative letters unless (s)he has already done that
* Hint user to hold shift for numlock unless (s)he has already done that
* Add an Activity that helps users enabling / choosing the new keyboard.

# TODO Before Publishing on Google Play
* Take keyboard layouts from the AOSP keyboard and let users choose one or more in a Settings
activity. Note that we most likely need to match their licensing terms for this.
* Test an English layout and see how that looks; it has shorter rows than Swedish and could look
bad.
* Change keyboard layout by swiping left or right with two fingers.
* Think about splitting the keyboard when wide enough
* Think about emoji support, or can we do that after Google Play?
* Make the letters look nicer by outlining them, shadowing them, anti aliasing them, bolding them,
switching font or something

# User Feedback
* "Hitting "ä" is too hard, maybe it's too close to the right edge?"
* "I prefer having all keys the same size"
* "I prefer a slanted layout, maybe we'd get that by having all keys the same size?"

# TODO Misc
* Remove all memory allocations from onDraw() and onMeasure() code paths. This is for avoiding GC
pauses during drawing operations.
* Think about keyboard transparency, especially on phones in landscape mode
* Think about sending accessibility events
* Enable users to change keyboard color
* Fix layout bounces, we do four layouts currently on Johan's phone

# DONE
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
