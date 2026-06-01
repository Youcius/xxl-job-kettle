# XXL-JOB Admin Design System

## Visual Theme & Atmosphere
- Mood: professional_minimal
- Feel: Clean, crisp, trustworthy — "冰雪聪明，一目了然"
- References: Arctic Frost palette, enterprise dashboards, Stripe Dashboard

## Color Palette & Roles
- Background: #FAFAFA — page background
- Surface: #FFFFFF — cards, sidebar, dialogs
- Text primary: #2C3E50 — headings, body text
- Text secondary: #8E9EAB — descriptions, placeholders
- Accent: #4A6FA5 (Steel Blue) — buttons, links, active states
- Accent hover: #3B5A85 — button hover, focus states
- Border: #E4E8EC — card borders, table lines
- Success: #56B37E
- Warning: #D4A245
- Danger: #C95B5B

## Typography Rules
- Display: system-ui, 700, clamp(1.5rem, 4vw, 2rem)
- Body: "PingFang SC", "Microsoft YaHei", "DejaVu Sans", sans-serif, 400, 1rem/1.6
- Mono: "DejaVu Sans Mono", "Courier New", monospace, 400, 0.813rem
- Code in log console uses Mono face; all other UI uses Body face

## Component Stylings
- Buttons: rounded-md (6px), accent bg for primary, light bg for default
- Cards: white surface, 1px solid border (#E4E8EC), 8px radius, subtle shadow (0 1px 3px rgba(0,0,0,0.04))
- Inputs: transparent bg, 1px solid border, 6px radius, focus ring accent
- Tables: header bg #F8F9FB, row hover #F5F7FA, stripe alternating
- Tags: small rounded, muted bg
- Sidebar: white bg, selected item accent-tinted bg (#E4EEF7), left border accent
- Topbar: white bg, 56px height, bottom border

## Layout Principles
- Max width: 100% (admin dashboard, no content max-width)
- Grid: two_column (sidebar 220px + content fluid)
- Sidebar: 220px expanded, 64px collapsed, collapsible
- Section spacing: 20px padding inside main content area
- Content padding: 20px

## Depth & Elevation
- Shadows: subtle — cards use 0 1px 3px rgba(0,0,0,0.04), dialogs use 0 4px 24px rgba(0,0,0,0.08)
- Borders: 1px solid #E4E8EC on cards and containers
- No decorative shadows beyond card/dialog elevation

## Do's and Don'ts
- DO use the declared color tokens exclusively (CSS variables prefixed `--el-`).
- DO maintain consistent 20px content padding.
- DO ensure all text meets WCAG AA contrast ratio.
- DO use accent color sparingly — buttons, links, active states only.
- DON'T invent colors outside this palette.
- DON'T add decorative shadows beyond card/dialog elevation.
- DON'T use more than 2 typefaces (body + mono for logs).

## Responsive Behavior
- Breakpoints: 768px (tablet), 1024px (desktop)
- Mobile (<768px): sidebar collapses to overlay, single column
- Desktop (≥1024px): full sidebar + content layout
- Table cells truncate with ellipsis on narrow screens

## Agent Prompt Guide
- All colors must reference CSS variables defined in theme.css
- Sidebar uses white bg with accent-tinted active state
- Blue primary (#4A6FA5) appears on: main buttons, active menu items, links, stat values
- Card style: white bg + 1px #E4E8EC border + 8px radius + subtle shadow
- Input style: 6px radius + focus ring
- All interactive elements need :focus-visible outline
- "冷冽干净，不可喧哗"
