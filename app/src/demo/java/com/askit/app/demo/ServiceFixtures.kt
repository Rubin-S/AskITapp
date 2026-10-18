package com.askit.app.demo

import com.askit.app.home.details.ServiceDetailData

/** Sample service details behind the sample Home services row. Debug/preview only. */
internal fun sampleServiceDetail(serviceId: String): ServiceDetailData? = when (serviceId) {
    "srv-1" -> ServiceDetailData(
        id = serviceId,
        title = "Emergency Plumbing & Drain Cleaning",
        category = "Plumbing",
        providerName = "Apex Plumbing Co.",
        providerId = "provider-apex",
        providerAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
        startingPrice = "From $75",
        rating = 4.9,
        reviewCount = 54,
        responseTime = "Responds in ~15 mins",
        coverageArea = "Metro Area (within 25 km)",
        description = "Full-service residential and emergency commercial plumbing. Specializing in drain cleaning, pipe repairs, water heater maintenance, faucet replacements, and video sewer line inspections. Available 24/7 with upfront transparent pricing.",
        inclusions = listOf(
            "Complete on-site diagnostic assessment",
            "Professional grade tools & camera inspection",
            "Upfront itemized estimate before work begins",
            "90-day comprehensive service warranty",
        ),
    )
    "srv-2" -> ServiceDetailData(
        id = serviceId,
        title = "Residential Electrical Repairs & Panels",
        category = "Electrical",
        providerName = "Bright Solutions",
        providerId = "provider-bright",
        providerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
        startingPrice = "From $90",
        rating = 4.8,
        reviewCount = 42,
        responseTime = "Responds in ~30 mins",
        coverageArea = "City & Surrounding Suburbs",
        description = "Certified master electricians specializing in residential circuit diagnostics, breaker box replacements, EV charger installations, and modern LED recessed lighting setups.",
        inclusions = listOf(
            "Electrical safety & code compliance inspection",
            "High-grade copper wiring and UL-listed breakers",
            "Clean workspace guarantee",
            "Full labor warranty & permit support",
        ),
    )
    "srv-3" -> ServiceDetailData(
        id = serviceId,
        title = "Custom Cabinetry & Woodworking",
        category = "Carpentry",
        providerName = "Heritage Woodworks",
        providerId = "provider-heritage",
        providerAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300",
        startingPrice = "From $150",
        rating = 4.95,
        reviewCount = 68,
        responseTime = "Responds within 1 hour",
        coverageArea = "Tri-County Area",
        description = "Bespoke hardwood furniture, custom built-in bookshelves, and kitchen cabinetry. Premium hardwoods sourced sustainably with expert joinery and hand-rubbed finishes.",
        inclusions = listOf(
            "In-home measurement and 3D design rendering",
            "Custom material & stain selection",
            "Precision milling and soft-close hardware",
            "Delivery and seamless installation",
        ),
    )
    else -> null
}
