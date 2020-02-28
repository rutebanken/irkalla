# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.12"
}

provider "google" {
  version = "~> 2.19"
}
provider "kubernetes" {
  config_context = var.kube_context
  version        = "~> 1.9"
}

# Create bucket
resource "google_storage_bucket" "storage_bucket" {
  name               = "${var.labels.team}-${var.labels.app}-${var.bucket_instance_suffix}"
  force_destroy      = var.force_destroy
  location           = var.location
  project            = var.gcp_project
  storage_class      = var.storage_class
  bucket_policy_only = var.bucket_policy_only
  labels             = var.labels

  versioning {
    enabled = var.versioning
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "${var.labels.team}-${var.labels.app}-${var.bucket_instance_suffix}"
  }
}

# create service account
resource "google_service_account" "storage_bucket_service_account" {
  account_id   = "ror-irkalla-sa"
  display_name = "ror-irkalla-sa service account"
  project = var.gcp_project
}

# add service account as member to the bucket
resource "google_storage_bucket_iam_member" "storage_bucket_iam_member" {
  bucket = google_storage_bucket.storage_bucket.name
  role   = var.service_account_bucket_role
  member = "serviceAccount:${google_service_account.storage_bucket_service_account.email}"
}

# add service account as member to the pubsub
resource "google_project_iam_member" "project" {
  project = var.gcp_project
  role    = var.service_account_pubsub_role
  member = "serviceAccount:${google_service_account.storage_bucket_service_account.email}"
}

# create key for service account
resource "google_service_account_key" "storage_bucket_service_account_key" {
  service_account_id = google_service_account.storage_bucket_service_account.name
}

  # Add SA key to to k8s
resource "kubernetes_secret" "storage_bucket_service_account_credentials" {
  metadata {
    name      = "ror-irkalla-sa-credentials"
    namespace = var.kube_namespace
  }
  data = {
    "credentials.json" = "${base64decode(google_service_account_key.storage_bucket_service_account_key.private_key)}"
  }
}