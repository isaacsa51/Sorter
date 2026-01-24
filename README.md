## Sorter

Media Sorter is a modern Android application designed to help users quickly clean up and organize their photo and video gallery. Swipe-based interface similar to popular card-swiping apps, users can efficiently sort through their media files and decide what to keep or delete.

#### Core Features
- Swipe-Based Sorting: Swipe down to keep photos/videos, swipe up to delete Safe
- Review System: Preview deleted items in a zoomable grid before permanent deletion
- Advanced Media Viewer: Pinch-to-zoom images, play videos with progress control, fullscreen mode
- Multi-Column Grid: Adjust grid density 1-4 columns with pinch gestures
- Material Design 3: Dynamic colors, smooth animations, predictive back gestures
- Customizable Settings: Light/Dark/System themes, Material You colors, blurred backgrounds
- Undo & Batch Actions: Restore individual items or delete all at once

#### Technical Highlights
- **Architecture:** MVVM with Clean Architecture principles
- **UI Framework:** Jetpack Compose with Material 3
- **Dependency Injection:** Hilt
- **State Management:** Kotlin Flow and StateFlow
- **Permissions:** Runtime media access permission handling
- **Media Loading:** Random batch loading for efficient performance
- **Animations:** Advanced Compose animations including shared transitions, predictive back, and custom gestures

> This app solves the common problem of cluttered photo galleries by
> making the cleanup process quick, fun, and safe with its modern,
> gesture-driven interface.
