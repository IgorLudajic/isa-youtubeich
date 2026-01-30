import Link from "next/link";
import { Suspense } from "react";
import HeaderAvatarButton from "@/components/HeaderAvatarButton";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Upload } from "@carbon/icons-react";
import { getProfile } from "@/lib/auth";

export default async function Header() {
  const profile = await getProfile();

  return (
    <>
      <header className="w-full bg-main top-0 fixed border-b-2 z-50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            <div className="flex items-center">
              <Link href="/" className="text-xl font-mono font-bold">
                Jutjubić
              </Link>
            </div>

            <nav className="flex items-center space-x-4">
              {profile && (
                <Link href="/upload">
                  <Button
                    variant="neutral"
                    size="icon"
                    className="hidden md:inline-flex"
                  >
                    <Upload className="size-5" />
                  </Button>
                </Link>
              )}

              <Suspense
                fallback={
                  <Skeleton className="rounded-full size-11 aspect-square" />
                }
              >
                <HeaderAvatarButton />
              </Suspense>
            </nav>
          </div>
        </div>
      </header>
      <div className="h-16" />
    </>
  );
}
