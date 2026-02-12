"use server";

import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";

export interface Profile {
  id?: string;
  email: string;
  name: string;
  surname: string;
  username: string;
  avatarUrl?: string;
}

export async function login(
  username: string,
  password: string,
  redirectUrl?: string,
): Promise<void> {
  const res = await fetch(`${process.env.API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!res.ok) {
    throw new Error("Login failed");
  }

  const { accessToken, expiresIn } = await res.json();
  const cookieStore = await cookies();
  cookieStore.set("token", accessToken, {
    maxAge: expiresIn / 1000,
    httpOnly: true,
    path: "/",
  });

  revalidatePath("/", "layout");
  redirect(redirectUrl || "/");
}

export async function getProfile(): Promise<Profile | null> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token) return null;

  const res = await fetch(`${process.env.API_BASE_URL}/auth/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!res.ok) return null;

  const user = await res.json();
  return {
    id: user.id.toString(),
    email: user.email,
    name: user.name,
    surname: user.surname,
    username: user.username,
    avatarUrl: user.avatarUrl,
  };
}

export async function signOut(): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.delete("token");
}

export async function signup(data: {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  name: string;
  surname: string;
  street: string;
  city: string;
  country: string;
}): Promise<void> {
  const res = await fetch(`${process.env.API_BASE_URL}/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    throw new Error("Signup failed");
  }
}

export async function getClientToken(): Promise<string | null> {
  const cookieStore = await cookies();
  return cookieStore.get("token")?.value || null;
}
