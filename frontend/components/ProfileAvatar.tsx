import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

interface ProfileAvatarProps {
  profile: {
    name: string;
    surname: string;
    avatarUrl?: string;
  };
  className?: string;
}

export default function ProfileAvatar({
  profile,
  className,
}: ProfileAvatarProps) {
  return (
    <Avatar className={className}>
      <AvatarImage src={profile.avatarUrl} alt={profile.name} />
      <AvatarFallback>
        {profile.name.charAt(0).toUpperCase() +
          profile.surname.charAt(0).toUpperCase()}
      </AvatarFallback>
    </Avatar>
  );
}
