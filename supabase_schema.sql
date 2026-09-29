-- =========================================================================
-- MINECRAFT BANGLADESH (MCBD ONE) - SUPABASE DATABASE SCHEMA
-- Run this in your Supabase SQL Editor:
-- Dashboard -> cvppveogubeudebsmazd -> SQL Editor -> New Query -> Run
-- =========================================================================

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- 1. PROFILES TABLE
create table if not exists public.profiles (
    id uuid primary key,
    email text,
    full_name text,
    avatar_url text,
    minecraft_ign text default 'SteveBD',
    rank text default 'Survivalist',
    status text default 'online',
    bio text default 'Minecraft Bangladesh Community Member 🇧🇩⛏️',
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

-- 2. CHANNELS TABLE
create table if not exists public.channels (
    id text primary key,
    name text not null,
    topic text,
    icon text not null,
    is_announcement boolean default false,
    created_at timestamptz default now()
);

-- 3. MESSAGES TABLE
create table if not exists public.messages (
    id uuid primary key default gen_random_uuid(),
    channel_id text not null references public.channels(id) on delete cascade,
    user_id uuid references public.profiles(id) on delete set null,
    user_name text not null,
    user_avatar text,
    minecraft_ign text default 'BD_Player',
    user_rank text default 'Survivalist',
    content text not null,
    image_url text,
    reply_to_id uuid,
    reply_to_content text,
    reply_to_sender text,
    reactions jsonb default '{}'::jsonb,
    created_at timestamptz default now()
);

-- 4. BD MINECRAFT SERVERS TABLE
create table if not exists public.servers (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    ip_address text not null,
    port integer default 25565,
    gamemode text not null,
    version text default '1.20.x - 1.21.x',
    online_players integer default 142,
    max_players integer default 500,
    ping_ms integer default 28,
    verified boolean default true,
    description text,
    created_at timestamptz default now()
);

-- 5. SHOWCASE POSTS TABLE (Builds & Redstone)
create table if not exists public.showcase_posts (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references public.profiles(id) on delete set null,
    user_name text not null,
    user_avatar text,
    minecraft_ign text,
    title text not null,
    description text,
    image_url text not null,
    likes_count integer default 12,
    category text default 'Mega Build',
    created_at timestamptz default now()
);

-- Enable Row Level Security (RLS)
alter table public.profiles enable row level security;
alter table public.channels enable row level security;
alter table public.messages enable row level security;
alter table public.servers enable row level security;
alter table public.showcase_posts enable row level security;

-- Policies for Profiles
create policy "Allow all users to read profiles" on public.profiles for select using (true);
create policy "Allow users to insert/update their profile" on public.profiles for all using (true) with check (true);

-- Policies for Channels
create policy "Allow all users to read channels" on public.channels for select using (true);
create policy "Allow insert on channels" on public.channels for insert with check (true);

-- Policies for Messages
create policy "Allow all users to read messages" on public.messages for select using (true);
create policy "Allow insert messages" on public.messages for insert with check (true);
create policy "Allow update reactions" on public.messages for update using (true);

-- Policies for Servers
create policy "Allow all users to read servers" on public.servers for select using (true);
create policy "Allow insert servers" on public.servers for insert with check (true);

-- Policies for Showcase
create policy "Allow all users to read showcase" on public.showcase_posts for select using (true);
create policy "Allow insert showcase" on public.showcase_posts for insert with check (true);

-- Realtime Publication for instant messaging
drop publication if exists supabase_realtime;
create publication supabase_realtime for table public.messages, public.profiles, public.servers, public.showcase_posts;

-- Seed Default Channels
insert into public.channels (id, name, topic, icon, is_announcement) values
('announcements', 'announcements', 'অফিশিয়াল বিডি টুর্নামেন্ট ও সার্ভার আপডেট', '📢', true),
('general', 'general-chat', 'বাংলাদেশি মাইনক্রাফটারদের আড্ডা ও খোশগল্প', '💬', false),
('pvp-bedwars', 'pvp-and-bedwars', 'বেডওয়ার্স স্কোয়াড ও পিভিপি ট্রিক্স', '⚔️', false),
('builds-redstone', 'builds-and-redstone', 'অসাধারণ বিল্ড ও রেডস্টোন মেশিনারি শেয়ার', '🧱', false),
('bd-servers', 'bd-server-ips', 'বাংলাদেশি সেরা সার্ভার আইপি ও লিস্ট', '🎮', false)
on conflict (id) do nothing;

-- Seed Welcome Message
insert into public.messages (channel_id, user_name, user_avatar, minecraft_ign, user_rank, content) values
('announcements', 'MCBD Admin 🇧🇩', 'https://crafthead.net/helm/Steve', 'MCBD_Staff', '👑 Admin', 'স্বাগতম Minecraft Bangladesh (MCBD ONE) অফিসিয়াল অ্যাপে! 🇧🇩🎮 এখানে সব বাংলাদেশি মাইনক্রাফটার একসাথে আড্ডা দিন, সার্ভার শেয়ার করুন ও টিম খুঁজুন!'),
('general', 'Tanvir_BD', 'https://crafthead.net/helm/Alex', 'TanvirCraft', '💎 Diamond Member', 'সবাই কেমন আছেন? আজকে রাতে কে কে Bedwars খেলবেন? নক দেন! ⚔️'),
('bd-servers', 'ServerMod', 'https://crafthead.net/helm/Notch', 'MCBD_Bot', '🛡️ Server Mod', 'বাংলাদেশি লো-পিং সার্ভার লিস্ট নিচে লাইভ দেখতে পাবেন! পিং চেক করে জয়েন করুন।')
on conflict do nothing;

-- Seed Verified Bangladesh Servers
insert into public.servers (name, ip_address, port, gamemode, version, online_players, max_players, ping_ms, verified, description) values
('MCBD Official SMP 🇧🇩', 'play.mcbd.network', 25565, 'Survival / Economy', '1.20 - 1.21', 184, 500, 18, true, 'অফিশিয়াল মাইনক্রাফট বাংলাদেশ সারভাইভাল সার্ভার। কাস্টম কোয়েস্ট ও লো-পিং।'),
('BD Bedwars Arena', 'bedwars.bdcraft.net', 25565, 'Bedwars / Skywars', '1.8 - 1.21', 96, 300, 24, true, 'দ্রুততম ম্যাচমেকিং ও বাংলাদেশি লিডারবোর্ড।'),
('Lifesteal BD SMP', 'lifesteal.banglacraft.xyz', 25565, 'Lifesteal SMP', '1.21.x', 67, 200, 29, true, 'হার্ডকোর লাইফস্টিল পিভিপি। হার্ট চুরি করুন এবং টিম গঠন করুন!')
on conflict do nothing;

-- 6. APP VERSIONS TABLE (In-App Version Control System)
create table if not exists public.app_versions (
    id uuid primary key default gen_random_uuid(),
    version_name text not null,
    version_code integer not null,
    min_supported_version text default '1.0.0',
    is_mandatory boolean default false,
    download_url text not null,
    changelog jsonb default '[]'::jsonb,
    release_date text default 'September 2026',
    created_at timestamptz default now()
);

alter table public.app_versions enable row level security;
create policy "Allow all users to read app_versions" on public.app_versions for select using (true);

-- Seed Initial App Version 1.0.0
insert into public.app_versions (version_name, version_code, min_supported_version, is_mandatory, download_url, changelog, release_date) values
('1.0.0', 1, '1.0.0', false, 'https://github.com/mcbdone/app/releases/tag/v1.0.0', '["Official launch of MCBD ONE 🇧🇩 (v1.0.0)", "White Themed Glassmorphic UI with dynamic ambient refraction", "Custom Minecraft 3D skin heads & rank system", "Bangladesh verified server list with live ping & player count", "Realtime community channels with emoji reactions", "Google Auth & Supabase Realtime synchronization"]'::jsonb, 'September 2026')
on conflict do nothing;

