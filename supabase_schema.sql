-- =========================================================================
-- MINECRAFT BANGLADESH (MCBD ONE) - COMPLETE FRESH START SQL SCRIPT
-- Run this in your Supabase SQL Editor:
-- Dashboard -> cvppveogubeudebsmazd -> SQL Editor -> New Query -> Paste ALL -> Run
-- =========================================================================

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- -------------------------------------------------------------------------
-- 1. CLEANUP PREVIOUS OBJECTS (100% IDEMPOTENT FRESH START)
-- -------------------------------------------------------------------------
-- Drop triggers & functions
drop trigger if exists on_auth_user_created on auth.users;
drop function if exists public.handle_new_user() cascade;

-- Drop any existing policies explicitly to prevent error 42710
do $$
begin
  -- Profiles policies
  drop policy if exists "Allow all users to read profiles" on public.profiles;
  drop policy if exists "Allow users to insert/update their profile" on public.profiles;
  drop policy if exists "Public profiles are viewable by everyone" on public.profiles;
  drop policy if exists "Users can insert their own profile" on public.profiles;
  drop policy if exists "Users can update own profile" on public.profiles;

  -- Channels policies
  drop policy if exists "Allow all users to read channels" on public.channels;
  drop policy if exists "Allow insert on channels" on public.channels;

  -- Messages policies
  drop policy if exists "Allow all users to read messages" on public.messages;
  drop policy if exists "Allow insert messages" on public.messages;
  drop policy if exists "Allow update reactions" on public.messages;

  -- Servers policies
  drop policy if exists "Allow all users to read servers" on public.servers;
  drop policy if exists "Allow insert servers" on public.servers;

  -- Showcase policies
  drop policy if exists "Allow all users to read showcase" on public.showcase_posts;
  drop policy if exists "Allow insert showcase" on public.showcase_posts;

  -- App versions policies
  drop policy if exists "Allow all users to read app_versions" on public.app_versions;
exception when others then
  null; -- Ignore if tables don't exist yet
end $$;

-- Drop all existing tables cleanly with CASCADE
drop table if exists public.messages cascade;
drop table if exists public.channels cascade;
drop table if exists public.servers cascade;
drop table if exists public.showcase_posts cascade;
drop table if exists public.app_versions cascade;
drop table if exists public.profiles cascade;

-- -------------------------------------------------------------------------
-- 2. CREATE FRESH TABLES
-- -------------------------------------------------------------------------

-- PROFILES (Stores user data, Google profile, Minecraft IGN, Rank)
create table public.profiles (
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

-- CHANNELS (Community channels: announcements, pvp, builds, etc.)
create table public.channels (
    id text primary key,
    name text not null,
    topic text,
    icon text not null,
    is_announcement boolean default false,
    created_at timestamptz default now()
);

-- MESSAGES (Realtime chat messages with replies & reactions)
create table public.messages (
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

-- SERVERS (Verified Bangladeshi Minecraft servers with live stats)
create table public.servers (
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

-- SHOWCASE POSTS (Community builds, mega projects, screenshots)
create table public.showcase_posts (
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

-- APP VERSIONS (In-App Strict Version Control & OTA System)
create table public.app_versions (
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

-- -------------------------------------------------------------------------
-- 3. ROW LEVEL SECURITY (RLS) & FRESH POLICIES
-- -------------------------------------------------------------------------
alter table public.profiles enable row level security;
alter table public.channels enable row level security;
alter table public.messages enable row level security;
alter table public.servers enable row level security;
alter table public.showcase_posts enable row level security;
alter table public.app_versions enable row level security;

-- PROFILES POLICIES
create policy "Allow all users to read profiles" on public.profiles for select using (true);
create policy "Allow users to insert/update their profile" on public.profiles for all using (true) with check (true);

-- CHANNELS POLICIES
create policy "Allow all users to read channels" on public.channels for select using (true);
create policy "Allow insert on channels" on public.channels for insert with check (true);

-- MESSAGES POLICIES
create policy "Allow all users to read messages" on public.messages for select using (true);
create policy "Allow insert messages" on public.messages for insert with check (true);
create policy "Allow update reactions" on public.messages for update using (true);

-- SERVERS POLICIES
create policy "Allow all users to read servers" on public.servers for select using (true);
create policy "Allow insert servers" on public.servers for insert with check (true);

-- SHOWCASE POLICIES
create policy "Allow all users to read showcase" on public.showcase_posts for select using (true);
create policy "Allow insert showcase" on public.showcase_posts for insert with check (true);

-- APP VERSIONS POLICIES
create policy "Allow all users to read app_versions" on public.app_versions for select using (true);

-- -------------------------------------------------------------------------
-- 4. GOOGLE AUTH SYNC TRIGGER
-- Automatically creates/syncs public.profiles when user signs in with Google
-- -------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, email, full_name, avatar_url, minecraft_ign, rank)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', 'MCBD Player'),
    coalesce(new.raw_user_meta_data->>'avatar_url', new.raw_user_meta_data->>'picture', 'https://crafthead.net/helm/Steve'),
    coalesce(split_part(new.email, '@', 1), 'SteveBD'),
    'Survivalist'
  )
  on conflict (id) do update set
    email = excluded.email,
    full_name = coalesce(excluded.full_name, public.profiles.full_name),
    avatar_url = coalesce(excluded.avatar_url, public.profiles.avatar_url),
    updated_at = now();
  return new;
end;
$$ language plpgsql security definer;

create trigger on_auth_user_created
  after insert or update on auth.users
  for each row execute function public.handle_new_user();

-- -------------------------------------------------------------------------
-- 5. REALTIME REPLICATION SETUP (SAFE FOR ALL SUPABASE PROJECTS)
-- -------------------------------------------------------------------------
do $$
begin
  alter publication supabase_realtime add table public.messages;
exception when others then null;
end $$;

do $$
begin
  alter publication supabase_realtime add table public.profiles;
exception when others then null;
end $$;

do $$
begin
  alter publication supabase_realtime add table public.servers;
exception when others then null;
end $$;

do $$
begin
  alter publication supabase_realtime add table public.showcase_posts;
exception when others then null;
end $$;

do $$
begin
  alter publication supabase_realtime add table public.app_versions;
exception when others then null;
end $$;

-- -------------------------------------------------------------------------
-- 6. SEED INITIAL DATA (VERSION 1.0.0)
-- -------------------------------------------------------------------------
insert into public.channels (id, name, topic, icon, is_announcement) values
('announcements', 'announcements', 'অফিশিয়াল বিডি টুর্নামেন্ট ও সার্ভার আপডেট', '📢', true),
('general', 'general-chat', 'বাংলাদেশি মাইনক্রাফটারদের আড্ডা ও খোশগল্প', '💬', false),
('pvp-bedwars', 'pvp-and-bedwars', 'বেডওয়ার্স স্কোয়াড ও পিভিপি ট্রিক্স', '⚔️', false),
('builds-redstone', 'builds-and-redstone', 'অসাধারণ বিল্ড ও রেডস্টোন মেশিনারি শেয়ার', '🧱', false),
('bd-servers', 'bd-server-ips', 'বাংলাদেশি সেরা সার্ভার আইপি ও লিস্ট', '🎮', false);


insert into public.app_versions (version_name, version_code, min_supported_version, is_mandatory, download_url, changelog, release_date) values
('1.0.0', 1, '1.0.0', false, 'https://github.com/mcbd-johanliebert/MCBD-One-Project/releases/tag/v1.0.0', 
'["Official launch of MCBD ONE 🇧🇩 (v1.0.0)", "White Themed Glassmorphic UI with dynamic ambient refraction", "Custom Minecraft 3D skin heads & rank system", "Bangladesh verified server list with live ping & player count", "Realtime community channels with emoji reactions", "Google Auth & Supabase Realtime synchronization"]'::jsonb, 
'September 2026');
