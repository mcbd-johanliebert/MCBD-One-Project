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
  drop policy if exists "Allow update on channels" on public.channels;
  drop policy if exists "Allow delete on channels" on public.channels;

  -- Messages policies
  drop policy if exists "Allow all users to read messages" on public.messages;
  drop policy if exists "Allow insert messages" on public.messages;
  drop policy if exists "Allow update reactions" on public.messages;

  -- Profiles policies
  drop policy if exists "Allow delete on profiles" on public.profiles;

  -- Servers policies
  drop policy if exists "Allow all users to read servers" on public.servers;
  drop policy if exists "Allow insert servers" on public.servers;
  drop policy if exists "Allow update on servers" on public.servers;
  drop policy if exists "Allow delete on servers" on public.servers;

  -- Showcase policies
  drop policy if exists "Allow all users to read showcase" on public.showcase_posts;
  drop policy if exists "Allow insert showcase" on public.showcase_posts;
  drop policy if exists "Allow update on showcase" on public.showcase_posts;
  drop policy if exists "Allow delete on showcase" on public.showcase_posts;

  -- App versions policies
  drop policy if exists "Allow all users to read app_versions" on public.app_versions;
  drop policy if exists "Allow insert on app_versions" on public.app_versions;
  drop policy if exists "Allow update on app_versions" on public.app_versions;
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
    bio text default 'Minecraft Bangladesh Community Member',
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
create policy "Allow delete on profiles" on public.profiles for delete using (true);

-- CHANNELS POLICIES
create policy "Allow all users to read channels" on public.channels for select using (true);
create policy "Allow insert on channels" on public.channels for insert with check (true);
create policy "Allow update on channels" on public.channels for update using (true);
create policy "Allow delete on channels" on public.channels for delete using (true);

-- MESSAGES POLICIES
create policy "Allow all users to read messages" on public.messages for select using (true);
create policy "Allow insert messages" on public.messages for insert with check (true);
create policy "Allow update reactions" on public.messages for update using (true);
create policy "Allow delete on messages" on public.messages for delete using (true);

-- SERVERS POLICIES
create policy "Allow all users to read servers" on public.servers for select using (true);
create policy "Allow insert servers" on public.servers for insert with check (true);
create policy "Allow update on servers" on public.servers for update using (true);
create policy "Allow delete on servers" on public.servers for delete using (true);

-- SHOWCASE POLICIES
create policy "Allow all users to read showcase" on public.showcase_posts for select using (true);
create policy "Allow insert showcase" on public.showcase_posts for insert with check (true);
create policy "Allow update on showcase" on public.showcase_posts for update using (true);
create policy "Allow delete on showcase" on public.showcase_posts for delete using (true);

-- APP VERSIONS POLICIES
create policy "Allow all users to read app_versions" on public.app_versions for select using (true);
create policy "Allow insert on app_versions" on public.app_versions for insert with check (true);
create policy "Allow update on app_versions" on public.app_versions for update using (true);

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
    case when lower(new.email) = 'nazmusshakibshihan@gmail.com' then 'Developer' else 'Member' end
  )
  on conflict (id) do update set
    email = excluded.email,
    full_name = coalesce(excluded.full_name, public.profiles.full_name),
    avatar_url = coalesce(excluded.avatar_url, public.profiles.avatar_url),
    rank = case when lower(excluded.email) = 'nazmusshakibshihan@gmail.com' then 'Developer' else public.profiles.rank end,
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
-- REALTIME DATABASE STORAGE & HEALTH RPC
-- Computes real-time PostgreSQL database disk usage & table row counts
-- -------------------------------------------------------------------------
create or replace function public.get_database_stats()
returns jsonb
language plpgsql
security definer
as $$
declare
    db_size_bytes bigint;
    users_count bigint;
    messages_count bigint;
    servers_count bigint;
    showcase_count bigint;
    channels_count bigint;
    calc_mb numeric;
    calc_pct numeric;
    health_status text;
begin
    select pg_database_size(current_database()) into db_size_bytes;
    select count(*) into users_count from public.profiles;
    select count(*) into messages_count from public.messages;
    select count(*) into servers_count from public.servers;
    select count(*) into showcase_count from public.showcase_posts;
    select count(*) into channels_count from public.channels;

    calc_mb := round((db_size_bytes::numeric / (1024 * 1024)), 2);
    calc_pct := round((calc_mb / 500.0) * 100.0, 1);

    if calc_pct >= 85.0 then
        health_status := 'CRITICAL';
    elsif calc_pct >= 65.0 then
        health_status := 'WARNING';
    else
        health_status := 'HEALTHY';
    end if;

    return json_build_object(
        'db_size_bytes', db_size_bytes,
        'db_size_mb', calc_mb,
        'max_storage_mb', 500.0,
        'storage_percent', calc_pct,
        'users_count', users_count,
        'messages_count', messages_count,
        'servers_count', servers_count,
        'showcase_count', showcase_count,
        'status', health_status
    );
end;
$$;

grant execute on function public.get_database_stats() to anon, authenticated;

-- -------------------------------------------------------------------------
-- 6. SEED INITIAL DATA (VERSION 1.0.2)
-- -------------------------------------------------------------------------
-- Channels are managed directly from the Developer Panel (clean fresh start)

-- Top Verified Bangladeshi Minecraft Servers
insert into public.servers (name, ip_address, port, gamemode, version, online_players, max_players, ping_ms, verified, description) values
('BD-Minecraft Community', 'mc.bd-mc.com', 25565, 'Survival / SMP', '1.20 - 1.21', 168, 500, 24, true, 'বাংলাদেশের অন্যতম জনপ্রিয় ও সুরক্ষিত সারভাইভাল এসএমপি সার্ভার। কাস্টম কোয়েস্ট ও ইকোনমি সুবিধা রয়েছে।'),
('BanglaCraft Network', 'play.banglacraft.net', 25565, 'Bedwars / Skywars', '1.8.x - 1.21.x', 312, 1000, 28, true, 'বিডির শীর্ষস্থানীয় পিভিপি নেটওয়ার্ক। আল্ট্রা-লো পিং ও কম্পিটিটিভ লিডারবোর্ড।'),
('Bengal SMP', 'smp.bengalmc.com', 25565, 'Lifesteal / Hardcore', '1.21.x', 94, 300, 32, true, 'অ্যাড্রেনালাইন রাশ লাইফস্টিল মেকানিক্স ও পিভিপি অ্যারেনা। হার্ডকোর প্লেয়ারদের জন্য আদর্শ।'),
('Dhaka Pixelverse', 'play.dhakapixel.com', 25565, 'Survival / Economy', '1.20+', 75, 250, 36, true, 'কমিউনিটি ফ্রেন্ডলি বিল্ডারদের জন্য পিভিপি-মুক্ত শান্ত নিরিবিলি পরিবেশ ও রিয়েল এস্টেট সিস্টেম।');

-- Official App Release (v1.0.2)
insert into public.app_versions (version_name, version_code, min_supported_version, is_mandatory, download_url, changelog, release_date) values
('1.0.2', 3, '1.0.0', false, 'https://github.com/mcbd-johanliebert/MCBD-One-Project/releases/tag/v1.0.2', 
'["MCBD ONE v1.0.2 Release", "Full root developer privileges across all channels, servers, builds, and user moderation", "Live server creation, verification toggling, and instant removal", "Community build showcase creator and direct content moderation", "Zero promotional ads/tournament banners - clean, focused UI", "Direct chat message deletion and real-time announcement broadcast", "Live 500MB Supabase storage health monitor and quota maintenance"]'::jsonb, 
'September 2026');

