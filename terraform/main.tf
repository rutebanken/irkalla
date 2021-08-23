# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.12"
}

provider "google" {
  version = "~> 3.0"
}
provider "kubernetes" {
  version = "~> 1.13.3"
  load_config_file = var.load_config_file
}

# Create bucket
resource "google_storage_bucket" "storage_bucket" {
  name               = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
  force_destroy      = var.force_destroy
  location           = var.location
  project            = var.storage_project
  storage_class      = var.storage_class
  bucket_policy_only = var.bucket_policy_only
  labels             = var.labels

  versioning {
    enabled = var.versioning
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
  }
}

# Create folder in a bucket
resource "google_storage_bucket_object" "content_folder" {
  name          = "StopPlace/"
  content       = "Not really a directory, but it's empty."
  bucket        = google_storage_bucket.storage_bucket.name
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
  project = var.pubsub_project
  role    = var.service_account_pubsub_role
  member = "serviceAccount:${google_service_account.storage_bucket_service_account.email}"
}

# pubsub topics ChouetteStopPlaceSyncQueue, ChouetteStopPlaceDeleteQueue

# Create pubsub topic
resource "google_pubsub_topic" "stopplace-sync-queue" {
  name   = "ChouetteStopPlaceSyncQueue"
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub subscription
resource "google_pubsub_subscription" "stopplace-sync-queue-subscription" {
  name  = "ChouetteStopPlaceSyncQueue"
  topic = google_pubsub_topic.stopplace-sync-queue.name
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub topic
resource "google_pubsub_topic" "stopplace-delete-queue" {
  name   = "ChouetteStopPlaceDeleteQueue"
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub subscription
resource "google_pubsub_subscription" "stopplace-delete-queue-subscription" {
  name  = "ChouetteStopPlaceDeleteQueue"
  topic = google_pubsub_topic.stopplace-delete-queue.name
  project = var.pubsub_project
  labels = var.labels
}

# create key for service account
resource "google_service_account_key" "storage_bucket_service_account_key" {
  service_account_id = google_service_account.storage_bucket_service_account.name
}

  # Add SA key to to k8s
resource "kubernetes_secret" "storage_bucket_service_account_credentials" {
  metadata {
    name      = "ror-irkalla-sa"
    namespace = var.kube_namespace
  }
  data = {
    "credentials.json" = "${base64decode(google_service_account_key.storage_bucket_service_account_key.private_key)}"
  }
}