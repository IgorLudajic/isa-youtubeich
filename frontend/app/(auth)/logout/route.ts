import { cookies } from "next/headers";

export async function GET() {
  const cookieStore = await cookies();
  cookieStore.delete("token");

  return Response.redirect(
    new URL(
      "/",
      new URL(process.env.NEXT_PUBLIC_BASE_URL || "http://localhost:3000"),
    ),
  );
}

export async function POST() {
  const cookieStore = await cookies();
  cookieStore.delete("token");

  return Response.redirect(
    new URL(
      "/",
      new URL(process.env.NEXT_PUBLIC_BASE_URL || "http://localhost:3000"),
    ),
  );
}
