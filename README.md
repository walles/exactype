# Exactype

Exactype is an Android keyboard focused on helping the user to type the right thing from the start
rather than correcting the user's mistakes.

Features (will) include:
* Large keys that are easy to hit
* Instant feedback on each key press on whether or not it was precise
* Gestures for common operations (including typing space)

# TODO Before Installing on Johan's Phone
* Find out why we have padding around the view and get rid of it
* Size the letters properly
* Insert characters in the text when the user presses the keys

# TODO Before Getting the First Beta Tester
* Make the letters look nicer by outlining them, shadowing them, anti aliasing them, bolding them,
switching font or something
* Implement a working SHIFT key
* Make the SHIFT key look like a SHIFT key
* Make the Backspace key look like a Backspace key
* Automatic SHIFT handling depending on what kind of field we're editing
* Make sure two SHIFT presses means ALL CAPS. Both when starting with lowercase letters and when
being automatically shifted.
* Test on different resolutions / screen sizes / screen rotations.
* Swipe right => insert a space
* Swipe right after a space => ". "
* Swipe right after ". " => "... "
* Make long pressing show the numeric keyboard for the duration of the long press
* Make long pressing "a" and keeping still show alternative "a"s like "@" after first showing the
numeric keyboard for a while
* Make long pressing SHIFT lock the numeric keyboard in place
* When sliding around the numeric keyboard, show what's under your finger in a small (fixed in
place) frame above the keyboard
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
* Change keyboard layout by swiping left or right with two fingers.
* Think about splitting the keyboard when wide enough
* Think about emoji support, or can we do that after Google Play?

# TODO Misc
* Think about keyboard transparency, especially on phones in landscape mode
* Think about sending accessibility events
* Enable users to change keyboard color

# DONE
* Create an IME that when selected shows an empty view
* Add licensing information (same as AOSP)
* Draw a Swedish keyboard in the view
* Size the keyboard and its keys properly and automatically
