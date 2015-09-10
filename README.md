# Exactype

Exactype is an Android keyboard focused on helping the user to type the right thing from the start
rather than correcting the user's mistakes.

Features (will) include:
* Large keys that are easy to hit
* Instant feedback on each key press on whether or not it was precise
* Gestures for common operations (including typing space)

# TODO
* Draw a Swedish keyboard in the view
* Insert characters in the text when the user presses the keys
* Implement a working SHIFT key
* Automatic SHIFT handling depending on what kind of field we're editing
* Make sure two SHIFT presses means ALL CAPS. Both when starting with lowercase letters and when
being automatically shifted.
* Swipe right => insert a space
* Swipe right after a space => ". "
* Swipe right after ". " => "... "
* Make long pressing show the numeric keyboard for the duration of the long press
* Make long pressing "a" and keeping still show alternative "a"s like "@" after first showing the
numeric keyboard for a while
* When sliding around the numeric keyboard, show what's under your finger in a small (fixed in
place) frame above the keyboard
* Add audio clicks on key presses
* Add vibrations on key presses
* Condition audio / vibration feedback on how close the hit was to the key we think the user
pressed.
* Take keyboard layouts from the AOSP keyboard and let users choose one or more in a Settings
activity. Note that we most likely need to match their licensing terms for this.
* Change keyboard layout by swiping left or right with two fingers.
* Think about splitting the keyboard when wide enough
* Think about keyboard transparency, especially on phones in landscape mode
* Add an Activity that helps users enabling / choosing the new keyboard.

# DONE
* Create an IME that when selected shows an empty view
