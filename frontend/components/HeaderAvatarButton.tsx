import { getProfile } from "@/lib/auth";
import LoginButton from "@/components/LoginButton";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Logout } from "@carbon/icons-react";
import Link from "next/link";

export default async function HeaderAvatarButton() {
  const profile = await getProfile();

  if (!profile) return <LoginButton />;

  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        <Avatar className={"cursor-pointer size-11"}>
          <AvatarImage src={profile.avatarUrl} alt={profile.name} />
          <AvatarFallback>
            {profile.name.charAt(0).toUpperCase() +
              profile.surname.charAt(0).toUpperCase()}
          </AvatarFallback>
        </Avatar>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56 bg-background" align="end">
        <DropdownMenuLabel className="select-none">Moj nalog</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <DropdownMenuItem asChild>
            <Link 
              href={`/profiles/${profile.username}`}
              className="w-full flex items-center cursor-pointer gap-2"
            >
              <span>Moj profil</span>
            </Link>
          </DropdownMenuItem>
        </DropdownMenuGroup>
        
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <form method="post" action="/logout">
            <DropdownMenuItem
              asChild
              className="bg-background hover:bg-red-600/50 hover:border-red-600 bg-red-400/60 border-black"
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