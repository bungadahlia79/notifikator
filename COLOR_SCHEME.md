# UI Color Scheme Reference

## Light Mode Colors

| Element | Color Code | Usage |
|---------|-----------|-------|
| Primary | #00FF00 | Action bar, primary buttons |
| Primary Dark | #00CC00 | Status bar |
| Accent | #03DAC6 | Category headers, toggles, highlights |
| Text Primary | #000000 | Main text, titles |
| Text Secondary | #5F6368 | Descriptions, hints |
| Background | #FAFAFA | Screen background |
| Card Background | #FFFFFF | List items, preferences |

## Dark Mode Colors

| Element | Color Code | Usage |
|---------|-----------|-------|
| Primary | #00FF00 | Action bar, primary buttons |
| Primary Dark | #00CC00 | Status bar |
| Accent | #03DAC6 | Category headers, toggles, highlights |
| Text Primary | #E0E0E0 | Main text, titles |
| Text Secondary | #A0A0A0 | Descriptions, hints |
| Background | #121212 | Screen background (OLED friendly) |
| Card Background | #1E1E1E | List items, preferences |

## Design Principles Applied

### 1. Modern Material Design
- Green-based primary palette (#00FF00 / #00FF00)
- Teal accent color (#03DAC6) for both modes
- Proper contrast ratios for accessibility (WCAG AA compliant)

### 2. Typography
- Sans-serif font family (default Android Roboto)
- Sans-serif-medium for headers and emphasis
- Letter spacing: 0.01-0.08 for improved readability

### 3. Spacing & Layout
- Consistent padding: 16dp horizontal, 12-16dp vertical
- List item spacing: 8dp between items
- Minimum touch targets: 48-64dp

### 4. Elevation & Depth
- Headers: 4dp elevation
- Cards: 2dp elevation
- Preference items: 1dp elevation
- Smooth shadows and depth perception

### 5. Responsive Design
- Portrait and landscape layouts
- Adaptive layouts for different screen sizes
- Values-sw320dp for smaller devices

## Key Features

✅ **Automatic Theme Switching**
- Follows system dark mode setting
- No manual configuration needed
- Instant theme updates

✅ **Modern Minimalist Design**
- Clean card-based layouts
- Reduced visual clutter
- Focus on content

✅ **Enhanced Readability**
- Optimized contrast ratios
- Proper text sizing
- Clear visual hierarchy

✅ **Smooth Interactions**
- Ripple effects on touch
- Material Design animations (built-in)
- Responsive feedback

✅ **OLED Optimization**
- True black (#121212) in dark mode
- Reduces screen burn-in
- Battery efficient on OLED displays
