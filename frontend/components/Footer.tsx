export default function Footer() {
  return (
    <footer className="w-full bg-background border-t-2">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-center">
          <p className="text-sm text-black dark:text-white">
            &copy; {new Date().getFullYear()} Jutjubić. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}
