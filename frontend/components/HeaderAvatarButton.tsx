import { getProfile } from "@/lib/auth";
import LoginButton from "@/components/LoginButton";
import ProfileAvatar from "@/components/ProfileAvatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Logout, User } from "@carbon/icons-react";
import Link from "next/link";

export default async function HeaderAvatarButton() {
  const profile = await getProfile();

  if (!profile) return <LoginButton />;

  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        <ProfileAvatar profile={profile} className={"cursor-pointer size-11"} />
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56 bg-background" align="end">
        <DropdownMenuLabel className="mb-2 flex items-center gap-2">
          <ProfileAvatar profile={profile} className="size-9 select-none" />
          <div className="flex flex-col">
            <span>
              {profile.name} {profile.surname}
            </span>
            <span className="font-mono font-semibold text-gray-500">
              @{profile.username}
            </span>
          </div>
        </DropdownMenuLabel>
        {/*<DropdownMenuSeparator />*/}
        <DropdownMenuGroup className="mb-1">
          <DropdownMenuItem
            asChild
            className="bg-background border-2 border-border hover:bg-main"
          >
            <Link
              href={`/profiles/${profile.username}`}
              className="w-full flex items-center cursor-pointer gap-2"
            >
              <User />
              <span>Moj profil</span>
            </Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild className="bg-main/20 border-2 border-border mb-1">
            <Link href="/testovi" className="w-full flex items-center cursor-pointer gap-2">
              <span className="font-bold text-xs font-mono tracking-tighter">SISTEMSKI TESTOVI</span>
            </Link>
          </DropdownMenuItem>
        </DropdownMenuGroup>

        {/*<DropdownMenuSeparator />*/}
        <DropdownMenuGroup>
          <form method="post" action="/logout">
            <DropdownMenuItem
              asChild
              className="hover:bg-red-600/50 hover:border-red-900 bg-red-400/60 border-black"
            >
              <button
                type="submit"
                className="w-full flex items-center cursor-pointer"
              >
                <Logout />
                <span>Izloguj se</span>
              </button>
            </DropdownMenuItem>
          </form>
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
