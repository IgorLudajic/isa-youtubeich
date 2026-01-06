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
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Avatar className={"cursor-pointer size-11"}>
          <AvatarImage src={profile.avatarUrl} alt={profile.name} />
          <AvatarFallback>
            {profile.name.charAt(0).toUpperCase() +
              profile.surname.charAt(0).toUpperCase()}
          </AvatarFallback>
        </Avatar>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56" align="end">
        <DropdownMenuLabel className="select-none">Moj nalog</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <Link className="contents" prefetch={false} href={"/logout"}>
            <DropdownMenuItem className="cursor-pointer">
              <Logout />
              <span>Izloguj se</span>
            </DropdownMenuItem>
          </Link>
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
