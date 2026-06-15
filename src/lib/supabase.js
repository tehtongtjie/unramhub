import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

// Tambahkan pengecekan agar tidak error jika .env belum terbaca
if (!supabaseUrl || !supabaseAnonKey) {
  console.error("Supabase URL atau Anon Key tidak ditemukan di .env");
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey)