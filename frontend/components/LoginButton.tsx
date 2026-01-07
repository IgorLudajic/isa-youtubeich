import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function LoginButton() {
  return (
    <Link href={"/login"}>
      <Button>Uloguj se</Button>
    </Link>
  );
}
