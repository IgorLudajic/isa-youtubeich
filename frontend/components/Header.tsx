import Link from "next/link";
import { Suspense } from "react";
import HeaderAvatarButton from "@/components/HeaderAvatarButton";
import { Skeleton } from "@/components/ui/skeleton";

export default function Header() {
  return (
    <>
      <header className="w-full bg-main top-0 fixed border-b-2 z-50">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            <div className="flex items-center">
              <Link href="/" className="text-xl font-mono">
                Jutjubić
              </Link>
            </div>
            <nav className="flex space-x-4">
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