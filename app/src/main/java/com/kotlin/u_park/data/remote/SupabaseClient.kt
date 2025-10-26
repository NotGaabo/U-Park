package com.kotlin.u_park.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://drjphklnugbafjcphhek.supabase.co",
    supabaseKey = "sb_publishable_cvLd0VSEk8UdKs-Dmr_Iuw_mclcVlUp"
) {
    install(Auth)
    install(Postgrest)
}