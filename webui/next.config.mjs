/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "standalone",
  typescript: {
    // FIXME: this is a workaround for now
    //ignoreBuildErrors: true,
  },
};

export default nextConfig;
