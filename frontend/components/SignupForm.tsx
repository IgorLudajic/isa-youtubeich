"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { signup } from "@/lib/auth";

interface SignupFormData {
  username: string;
  email: string;
  password: string;
  passwordConfirm: string;
  name: string;
  surname: string;
  street: string;
  city: string;
  country: string;
}

export default function SignupForm() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("account");
  const {
    register,
    formState: { errors },
    watch,
    trigger,
    getValues,
    setFocus,
  } = useForm<SignupFormData>({
    shouldUseNativeValidation: false,
  });

  const onSubmit = async (data: SignupFormData) => {
    setIsSubmitting(true);
    try {
      await signup(data);
      setSuccessMessage(
        "Uspešna registracija! Poslali smo vam mejl sa uputstvima kako da aktivirate svoj nalog kako biste mogli da se ulogujete.",
      );
      setErrorMessage(null);
    } catch (error) {
      // TODO: differentiate error messages based on error type
      console.error("Registracija neuspela:", error);
      setErrorMessage("Registracija neuspela. Molimo pokušajte ponovo.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleNext = async () => {
    const isValid = await trigger([
      "username",
      "email",
      "password",
      "passwordConfirm",
    ]);
    if (isValid) {
      setActiveTab("personal");
    }
  };

  const handleSubmitClick = async () => {
    setErrorMessage(null);
    const isValid = await trigger();
    if (isValid) {
      await onSubmit(getValues());
    } else {
      const errorFields = Object.keys(errors);
      if (errorFields.length > 0) {
        const firstError = errorFields[0];
        const tab = [
          "username",
          "email",
          "password",
          "passwordConfirm",
        ].includes(firstError)
          ? "account"
          : "personal";
        setActiveTab(tab);
        setFocus(firstError as keyof SignupFormData);
      }
    }
  };

  if (successMessage) {
    return (
      <Card className="max-w-100">
        <CardHeader>
          <CardTitle>Uspešna registracija</CardTitle>
        </CardHeader>
        <CardContent>
          <p>{successMessage}</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="max-w-100">
      <Tabs
        defaultValue="account"
        className="w-full"
        value={activeTab}
        onValueChange={setActiveTab}
      >
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="account">Nalog</TabsTrigger>
          <TabsTrigger value="personal">Lični podaci</TabsTrigger>
        </TabsList>
        <TabsContent value="account" forceMount>
          <Card className={activeTab === "account" ? "" : "hidden"}>
            <CardHeader>
              <CardTitle>Podaci o nalogu</CardTitle>
              <CardDescription>
                Unesti osnovne podatke o Vašem novom nalogu.
              </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4">
              <div className="grid gap-2">
                <Label htmlFor="username">Korisničko ime</Label>
                <Input
                  id="username"
                  disabled={isSubmitting}
                  {...register("username", {
                    required: "Korisničko ime je obavezno",
                  })}
                />
                {errors.username && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.username.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="email">Mejl</Label>
                <Input
                  id="email"
                  type="email"
                  disabled={isSubmitting}
                  {...register("email", { required: "Mejl je obavezan" })}
                />
                {errors.email && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.email.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="password">Lozinka</Label>
                <Input
                  id="password"
                  type="password"
                  disabled={isSubmitting}
                  {...register("password", {
                    required: "Lozinka je neophodna",
                    minLength: {
                      value: 8,
                      message: "Lozinka mora biti barem 8 karaktera duga",
                    },
                  })}
                />
                {errors.password && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.password.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="passwordConfirm">Potvrdite lozinku</Label>
                <Input
                  id="passwordConfirm"
                  type="password"
                  disabled={isSubmitting}
                  {...register("passwordConfirm", {
                    required: "Molimo potvrdite lozinku",
                    validate: (value) =>
                      value === watch("password") || "Lozinke se ne poklapaju",
                  })}
                />
                {errors.passwordConfirm && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.passwordConfirm.message}
                  </p>
                )}
              </div>
            </CardContent>
            <CardFooter>
              <Button
                type="button"
                className="w-full"
                onClick={handleNext}
                disabled={isSubmitting}
              >
                Dalje
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
        <TabsContent value="personal" forceMount>
          <Card className={activeTab === "personal" ? "" : "hidden"}>
            <CardHeader>
              <CardTitle>Lični podaci</CardTitle>
              <CardDescription>
                Unesite Vaše lične podatke za kreiranje naloga.
              </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4">
              <div className="grid gap-2">
                <Label htmlFor="name">Ime</Label>
                <Input
                  id="name"
                  disabled={isSubmitting}
                  {...register("name", { required: "Ime je obavezno" })}
                />
                {errors.name && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.name.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="surname">Prezime</Label>
                <Input
                  id="surname"
                  disabled={isSubmitting}
                  {...register("surname", {
                    required: "Prezime je obavezno",
                  })}
                />
                {errors.surname && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.surname.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="street">Ulica</Label>
                <Input
                  id="street"
                  disabled={isSubmitting}
                  {...register("street", { required: "Ulica je obavezna" })}
                />
                {errors.street && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.street.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="city">Grad</Label>
                <Input
                  id="city"
                  disabled={isSubmitting}
                  {...register("city", { required: "Grad je obavezan" })}
                />
                {errors.city && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.city.message}
                  </p>
                )}
              </div>
              <div className="grid gap-2">
                <Label htmlFor="country">Država</Label>
                <Input
                  id="country"
                  disabled={isSubmitting}
                  {...register("country", { required: "Država je obavezna" })}
                />
                {errors.country && (
                  <p className="text-red-700 text-sm -mt-1 font-normal select-none">
                    {errors.country.message}
                  </p>
                )}
              </div>
            </CardContent>
            <CardFooter>
              <Button
                type="button"
                className="w-full"
                onClick={handleSubmitClick}
                disabled={isSubmitting}
              >
                Registrujte se
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
      {errorMessage && (
        <Card className="max-w-100 mt-4 bg-rose-100">
          <CardHeader>
            <CardTitle>Greška</CardTitle>
          </CardHeader>
          <CardContent>
            <p>{errorMessage}</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
