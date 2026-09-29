# 🇧🇩 MCBD ONE — Minecraft Bangladesh Community App

**MCBD ONE** is the official community messaging and server discovery app built for **Minecraft Bangladesh**, designed with a **White-Themed Glassmorphism** visual system, powered by **Supabase Database & Realtime**, and authenticated exclusively via **Google Sign-In**.

---

## 🌟 Key Features

1. **Ultra-Modern White Glassmorphic UI**:
   - Translucent frosted glass surfaces with specular highlights and refraction glow.
   - Dynamic ambient background mesh with glowing Aurora Emerald and Cyan Diamond orbs.
   - Frosted glass chat bubbles (Sender: emerald mint glass; Receiver: milk pearl glass).
   - Floating glass top navigation and bottom pill menu.
   - Pixel-perfect typography and Minecraft Bangladesh iconography.

2. **Google Authentication Only**:
   - Google Sign-In with Credential Manager API.
   - Direct ID token exchange with Supabase Auth (`/auth/v1/token?grant_type=id_token`).
   - Automatic Minecraft profile creation (`profiles` table).

3. **Community Channels & Realtime Chat**:
   - `# 📢 announcements` — Official MCBD tournaments, events, and server updates.
   - `# 💬 general-chat` — Community hangout and discussions.
   - `# ⚔️ pvp-bedwars` — Bedwars, Skywars squads and combat tactics.
   - `# 🧱 builds-redstone` — Base showcases and redstone machinery.
   - `# 🎮 bd-servers` — Bangladeshi low-ping server IP sharing and live status.

4. **Minecraft Integration & Custom Avatars**:
   - Real-time Minecraft head avatar rendering using Crafthead/Minotar.
   - Dynamic player rank badges: `👑 Admin`, `🛡️ Mod`, `💎 Diamond Member`, `⚡ Redstoner`, `⚔️ PvP Master`, `⛏️ Survivalist`.
   - In-app Minecraft IGN editor that synchronizes with player skin heads.

5. **BD Minecraft Server Hub**:
   - Live verified server list (`play.mcbd.network`, `bedwars.bdcraft.net`, `lifesteal.banglacraft.xyz`).
   - One-tap IP copy to clipboard.
   - Live player count and Bangladesh low-ping indicator.

6. **Builds & Redstone Showcase**:
   - Community gallery with screenshot previews and category filters.
   - Interactive heart likes and Minecraft version tags.

---

## 🔑 Project & Keystore Credentials

| Parameter | Value |
| :--- | :--- |
| **Package Name** | `com.mcbdone.app` |
| **Supabase Project URL** | `https://cvppveogubeudebsmazd.supabase.co` |
| **Google Web Client ID** | Configured in `app/build.gradle.kts` |
| **Google Android Client ID** | Configured in `app/build.gradle.kts` |
| **SHA-1 Fingerprint** | `3D:4A:E1:18:95:1E:B3:9D:0D:21:36:97:60:1B:88:E5:91:43:C3:75` |
| **SHA-256 Fingerprint** | `72:9D:8E:8F:72:32:7D:09:D2:62:67:02:E0:DD:53:EE:83:DF:EE:80:92:22:CA:C8:83:B2:3A:57:12:20:C3:17` |

---

## 🗄️ Supabase Database Setup

1. Open your Supabase Dashboard: [https://supabase.com/dashboard/project/cvppveogubeudebsmazd](https://supabase.com/dashboard/project/cvppveogubeudebsmazd)
2. Go to **SQL Editor** -> **New Query**.
3. Open the [`supabase_schema.sql`](supabase_schema.sql) file included in this repository.
4. Paste the content into the SQL Editor and click **Run**.
5. Go to **Authentication** -> **Providers** -> **Google**:
   - Enable Google Provider.
   - Enter your **Client ID (Web)** and **Client Secret** from Google Cloud Console.
   - Save.

---

## 🚀 Building & Running the App

### In Android Studio
1. Open Android Studio.
2. Select **Open** and choose this folder: `c:\Users\nazmu\Downloads\MCBD One Project`.
3. Allow Gradle to sync.
4. Select your target device or emulator and press **Run** (`Shift + F10`).
